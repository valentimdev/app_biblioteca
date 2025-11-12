// BookDetailFragment.kt
package com.example.bibliotecaunifor.ui.book

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.utils.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private var isAdmin: Boolean = false

    companion object {
        fun newInstance(book: Book, isAdmin: Boolean = false): BookDetailFragment {
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
                putString("adminId", book.adminId)
                putBoolean("isAdmin", isAdmin)
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
                adminId = it.getString("adminId") ?: ""
            )
            isAdmin = it.getBoolean("isAdmin", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_book_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.txtTitle).text = book.title
        view.findViewById<TextView>(R.id.txtAuthor).text = "Autor: ${book.author}"
        view.findViewById<TextView>(R.id.txtIsbn).text = "ISBN: ${book.isbn ?: "-"}"
        view.findViewById<TextView>(R.id.txtDescription).text = "Descrição: ${book.description ?: "-"}"
        view.findViewById<TextView>(R.id.txtTotalCopies).text =
            "Cópias: ${book.availableCopies} / ${book.totalCopies} disponíveis"

        val btnAction = view.findViewById<Button>(R.id.btnAction)

        if (isAdmin) {
            btnAction.text = "GERENCIAR ESTOQUE"
            btnAction.setOnClickListener { showAdminInfo() }
        } else {
            if (book.availableCopies > 0) {
                btnAction.text = "ALUGAR AGORA"
                btnAction.setOnClickListener { showUserRentInfo() }
            } else {
                btnAction.text = "INDISPONÍVEL"
                btnAction.isEnabled = false
            }
        }
    }

    private fun showAdminInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Estoque do livro")
            .setMessage("Total: ${book.totalCopies}\nDisponíveis: ${book.availableCopies}")
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun showUserRentInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Alugar livro")
            .setMessage("Você deseja alugar \"${book.title}\"?\n\nDevolução em 7 dias.")
            .setPositiveButton("Sim") { _, _ -> rentBook() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun rentBook() {
        val token = getAuthToken() ?: return showError("Faça login")
        val body = mapOf("dueDate" to getDueDateIn7Days())

        RetrofitClient.bookApi.rentBook(book.id, body, token)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        Toast.makeText(context, "Empréstimo solicitado!", Toast.LENGTH_SHORT).show()
                        (requireActivity() as? MainActivity)?.refreshHomeFragment()
                        parentFragmentManager.popBackStack()
                    } else {
                        val error = response.errorBody()?.string() ?: "Erro desconhecido"
                        showError("Falha: $error")
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    showError("Sem conexão")
                }
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