package com.example.bibliotecaunifor.fragment

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.CatalogUserFragment
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.models.UserResponse
import com.example.bibliotecaunifor.utils.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private var userHasRental: Boolean = false

    companion object {
        fun newInstance(book: Book, userHasRental: Boolean = false): BookDetailFragment {
            val frag = BookDetailFragment()
            val args = Bundle().apply {
                putString("id", book.id)
                putString("createdAt", book.createdAt)
                putString("updatedAt", book.updatedAt)
                putString("title", book.title)
                putString("author", book.author)
                putString("isbn", book.isbn ?: "")
                putString("description", book.description ?: "")
                putInt("totalCopies", book.totalCopies)
                putInt("availableCopies", book.availableCopies)
                putBoolean("userHasRental", userHasRental)
                putString("imageUrl", book.imageUrl)
            }
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            book = Book(
                id = it.getString("id") ?: "",
                createdAt = it.getString("createdAt") ?: "",
                updatedAt = it.getString("updatedAt") ?: "",
                title = it.getString("title") ?: "",
                author = it.getString("author") ?: "",
                isbn = it.getString("isbn"),
                description = it.getString("description"),
                totalCopies = it.getInt("totalCopies"),
                availableCopies = it.getInt("availableCopies"),
                imageUrl = it.getString("imageUrl")
            )
            userHasRental = it.getBoolean("userHasRental", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_book_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtAuthor = view.findViewById<TextView>(R.id.txtAuthor)
        val txtIsbn = view.findViewById<TextView>(R.id.txtIsbn)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val txtTotalCopies = view.findViewById<TextView>(R.id.txtTotalCopies)
        val txtRentedWarning = view.findViewById<TextView>(R.id.txtRentedWarning)
        val btnAction = view.findViewById<Button>(R.id.btnAction)

        txtTitle.text = book.title
        txtAuthor.text = "Autor: ${book.author}"
        txtIsbn.text = "ISBN: ${book.isbn ?: "-"}"
        txtDescription.text = "Descrição: ${book.description ?: "-"}"
        txtTotalCopies.text = "Cópias: ${book.availableCopies} / ${book.totalCopies} disponíveis"

        if (userHasRental) {
            txtRentedWarning.visibility = View.VISIBLE
            txtRentedWarning.text = "Você já possui empréstimo com esse livro"
        } else {
            txtRentedWarning.visibility = View.GONE
        }

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

    private fun rentBook() {
        val token = getAuthToken() ?: return showError("Faça login")

        RetrofitClient.userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    showError("Falha ao obter dados do usuário")
                    return
                }

                val userId = response.body()!!.id
                val body = mapOf(
                    "userId" to userId,
                    "bookId" to book.id,
                    "dueDate" to getDueDateIn7Days()
                )

                Log.d("BookDetailFragment", "Aluguel body: $body")
                Log.d("BookDetailFragment", "Token: $token")

                RetrofitClient.rentalApi.rentBook(body, token)
                    .enqueue(object : Callback<Rental> {
                        override fun onResponse(call: Call<Rental>, response: Response<Rental>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Empréstimo realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                val btnAction = view?.findViewById<Button>(R.id.btnAction)
                                btnAction?.text = "DEVOLVER AGORA"
                                btnAction?.setOnClickListener { returnBook() }

                                (requireActivity() as? MainActivity)?.refreshHomeFragment()
                                (requireActivity().supportFragmentManager.findFragmentByTag("catalog") as? CatalogUserFragment)?.refreshCatalog()
                            } else {
                                val error = response.errorBody()?.string() ?: "Erro desconhecido"
                                showError("Falha ao alugar livro: $error")
                            }
                        }

                        override fun onFailure(call: Call<Rental>, t: Throwable) {
                            showError("Sem conexão: ${t.message}")
                        }
                    })
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                showError("Falha ao obter usuário: ${t.message}")
            }
        })
    }

    private fun returnBook() {
        val token = getAuthToken() ?: return showError("Faça login")

        RetrofitClient.bookApi.returnBook(book.id, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(context, "Livro devolvido!", Toast.LENGTH_SHORT).show()
                    (requireActivity() as? MainActivity)?.refreshHomeFragment()
                    parentFragmentManager.popBackStack()
                } else {
                    val error = response.errorBody()?.string() ?: "Erro desconhecido"
                    showError("Falha: $error")
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) { showError("Sem conexão") }
        })
    }

    private fun getDueDateIn7Days(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    private fun getAuthToken(): String? {
        val context = context ?: return null
        val token = AuthUtils.getToken(context)
        return if (token.isNullOrBlank()) null else "Bearer $token"
    }

    private fun showError(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
