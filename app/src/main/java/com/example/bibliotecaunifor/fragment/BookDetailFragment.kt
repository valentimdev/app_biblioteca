package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private var userHasRental: Boolean = false

    companion object {
        fun newInstance(book: Book, userHasRental: Boolean = false): BookDetailFragment {
            return BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("book", book)
                    putBoolean("userHasRental", userHasRental)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            book = it.getParcelable("book") ?: Book("", "", "", "", "", "", "", 0, 0, "")
            userHasRental = it.getBoolean("userHasRental", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_book_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esconde o BottomNavigation só nessa tela
        (activity as? MainActivity)?.hideBottomNav()

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtAuthor = view.findViewById<TextView>(R.id.txtAuthor)
        val txtIsbn = view.findViewById<TextView>(R.id.txtIsbn)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val txtCopies = view.findViewById<TextView>(R.id.txtTotalCopies)
        val txtRentedWarning = view.findViewById<TextView>(R.id.txtRentedWarning)
        val btnAction = view.findViewById<Button>(R.id.btnAction)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        // Botão de voltar
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Preenche os dados
        txtTitle.text = book.title
        txtAuthor.text = "Autor: ${book.author}"
        txtIsbn.text = "ISBN: ${book.isbn ?: "-"}"
        txtDescription.text = book.description ?: "Sem descrição"
        txtCopies.text = "Cópias disponíveis: ${book.availableCopies} de ${book.totalCopies}"

        // Aviso se já está alugado
        if (userHasRental) {
            txtRentedWarning.visibility = View.VISIBLE
            txtRentedWarning.text = "Você já possui este livro alugado"
        } else {
            txtRentedWarning.visibility = View.GONE
        }

        // Configura botão de ação
        when {
            userHasRental -> {
                btnAction.text = "DEVOLVER AGORA"
                btnAction.setOnClickListener { returnBook() }
            }
            book.availableCopies > 0 -> {
                btnAction.text = "ALUGAR AGORA"
                btnAction.setOnClickListener { rentBook() }
            }
            else -> {
                btnAction.text = "INDISPONÍVEL"
                btnAction.isEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        // Mostra o BottomNavigation de novo ao sair da tela de detalhes
        (activity as? MainActivity)?.showBottomNav()
        super.onDestroyView()
    }

    private fun safeToast(message: String) {
        if (isAdded && context != null && message.isNotBlank()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun rentBook() {
        // Gera a data de devolução: hoje + 7 dias (formato ISO UTC)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val dueDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(calendar.time)

        val body = mapOf("dueDate" to dueDate)

        RetrofitClient.bookApi.rentBookWithDueDate(book.id, body)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        safeToast("Livro alugado com sucesso!")
                        updateUiAfterRent()
                        refreshOtherFragments()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Livro indisponível ou já alugado por você"
                            403 -> "Você já possui este livro alugado"
                            else -> "Erro ao alugar. Tente novamente."
                        }
                        safeToast(errorMsg)
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    safeToast("Sem conexão com a internet")
                }
            })
    }

    private fun returnBook() {
        RetrofitClient.bookApi.returnBook(book.id)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        safeToast("Livro devolvido com sucesso!")
                        parentFragmentManager.popBackStack()
                        refreshOtherFragments()
                    } else {
                        safeToast("Erro ao devolver")
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    safeToast("Sem conexão")
                }
            })
    }

    private fun updateUiAfterRent() {
        userHasRental = true
        view?.findViewById<TextView>(R.id.txtRentedWarning)?.apply {
            visibility = View.VISIBLE
            text = "Você já possui este livro alugado"
        }
        view?.findViewById<Button>(R.id.btnAction)?.apply {
            text = "DEVOLVER AGORA"
            setOnClickListener { returnBook() }
        }
    }

    private fun refreshOtherFragments() {
        (requireActivity() as? MainActivity)?.refreshHomeFragment()
        parentFragmentManager.fragments.forEach { frag ->
            (frag as? CatalogUserFragment)?.refreshCatalog()
        }
    }
}
