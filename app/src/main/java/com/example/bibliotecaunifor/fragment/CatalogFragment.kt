package com.example.bibliotecaunifor.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.BookDetailFragment
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.CreateBookDto
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val allBooks = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter
    private var isAdmin = false // ou setar conforme login

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookAdapter(allBooks, isAdmin) { action, book ->
            when (action) {
                "detail" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, isAdmin))
                    .addToBackStack(null)
                    .commit()
                "edit" -> if (isAdmin) showAddDialog(book)
                "remove" -> if (isAdmin) removeBook(book)
            }
        }
        rv.adapter = adapter

        val edtSearch = view.findViewById<EditText>(R.id.edtSearch)
        edtSearch.doAfterTextChanged { text ->
            val filtered = allBooks.filter {
                it.title.contains(text.toString(), ignoreCase = true) ||
                        it.author.contains(text.toString(), ignoreCase = true)
            }
            adapter.updateData(filtered)
        }

        fetchBooks()
    }

    private fun fetchBooks() {
        val tokenStr = AuthUtils.getToken(requireContext())
        if (tokenStr.isNullOrEmpty()) {
            android.util.Log.e("CatalogFragment", "Token nulo!")
            return
        }
        val token = "Bearer $tokenStr"

        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                android.util.Log.d("CatalogFragment", "Response code: ${response.code()}")
                android.util.Log.d("CatalogFragment", "Body: ${response.body()}")

                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    adapter.updateData(allBooks)
                } else {
                    android.util.Log.e(
                        "CatalogFragment",
                        "Erro ao buscar livros: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                android.util.Log.e("CatalogFragment", "Falha na requisição", t)
            }
        })
    }

    private fun showAddDialog(book: Book? = null) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_book, null)
        val edtTitulo = layout.findViewById<EditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<EditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<EditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<EditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<EditText>(R.id.edtTotalCopies)

        if (book != null) {
            edtTitulo.setText(book.title)
            edtAutor.setText(book.author)
            edtIsbn.setText(book.isbn ?: "")
            edtDescricao.setText(book.description ?: "")
            edtTotalCopies.setText(book.totalCopies.toString())
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (book == null) "Adicionar Livro" else "Editar Livro")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val token = "Bearer ${AuthUtils.getToken(requireContext())}"

                if (book == null) {
                    val dto = CreateBookDto(
                        title = edtTitulo.text.toString(),
                        author = edtAutor.text.toString(),
                        isbn = edtIsbn.text.toString(),
                        description = edtDescricao.text.toString(),
                        totalCopies = edtTotalCopies.text.toString().toIntOrNull() ?: 0
                    )
                    RetrofitClient.bookApi.createBook(dto, token).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) { fetchBooks() }
                        override fun onFailure(call: Call<Book>, t: Throwable) { t.printStackTrace() }
                    })
                } else {
                    val dto = EditBookDto(
                        title = edtTitulo.text.toString(),
                        author = edtAutor.text.toString(),
                        isbn = edtIsbn.text.toString(),
                        description = edtDescricao.text.toString(),
                        totalCopies = edtTotalCopies.text.toString().toIntOrNull()
                    )
                    RetrofitClient.bookApi.updateBook(book.id, dto, token).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) { fetchBooks() }
                        override fun onFailure(call: Call<Book>, t: Throwable) { t.printStackTrace() }
                    })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removeBook(book: Book) {
        val token = "Bearer ${AuthUtils.getToken(requireContext())}"
        RetrofitClient.bookApi.deleteBook(book.id, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) { fetchBooks() }
            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) { t.printStackTrace() }
        })
    }
}
