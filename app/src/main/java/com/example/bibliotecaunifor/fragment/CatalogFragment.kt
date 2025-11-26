package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private lateinit var rvBooks: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var adapter: BookAdapter

    private val allBooks = mutableListOf<Book>()
    private var showOnlyAvailable = false
    private var userRentedBookIds = emptyList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBooks = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookAdapter(allBooks, false) { action, book ->
            if (action == "detail") {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, userRentedBookIds.contains(book.id)))
                    .addToBackStack(null)
                    .commit()
            }
        }
        rvBooks.adapter = adapter

        btnFilter.setOnClickListener { showFilterDialog() }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }

        // Carrega tudo ao abrir
        loadUserRentalsAndBooks()
    }

    private fun loadUserRentalsAndBooks() {
        // Primeiro busca os livros alugados pelo usuário
        RetrofitClient.bookApi.getMyRentals().enqueue(object : Callback<List<com.example.bibliotecaunifor.models.Rental>> {
            override fun onResponse(call: Call<List<com.example.bibliotecaunifor.models.Rental>>, response: Response<List<com.example.bibliotecaunifor.models.Rental>>) {
                userRentedBookIds = response.body()
                    ?.filter { it.returnDate == null }
                    ?.mapNotNull { it.bookId }
                    ?: emptyList()

                // Depois busca todos os livros
                fetchBooks()
            }

            override fun onFailure(call: Call<List<com.example.bibliotecaunifor.models.Rental>>, t: Throwable) {
                userRentedBookIds = emptyList()
                fetchBooks()
            }
        })
    }

    private fun fetchBooks() {
        RetrofitClient.bookApi.getBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    val books = response.body() ?: emptyList()
                    books.forEach { book ->
                        book.isRentedByUser = userRentedBookIds.contains(book.id)
                    }
                    allBooks.addAll(books)
                    applyFilterAndSearch()
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                // Silencioso — não quebra a tela
            }
        })
    }

    private fun showFilterDialog() {
        val options = arrayOf("Todos os livros", "Apenas disponíveis")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar livros")
            .setItems(options) { _, which ->
                showOnlyAvailable = which == 1
                applyFilterAndSearch()
            }
            .show()
    }

    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        if (query.isBlank() && !showOnlyAvailable) {
            adapter.updateData(allBooks)
            return
        }

        val lowerQuery = query.lowercase()

        val filtered = allBooks.filter { book ->
            val matchesSearch = query.isBlank() ||
                    book.title.lowercase().contains(lowerQuery) ||
                    book.author.lowercase().contains(lowerQuery) ||
                    (book.isbn?.lowercase()?.contains(lowerQuery) == true)

            val isAvailable = !showOnlyAvailable || book.availableCopies > 0

            matchesSearch && isAvailable
        }

        adapter.updateData(filtered)
    }

    // Chamado após alugar/devolver um livro
    fun refreshCatalog() {
        loadUserRentalsAndBooks()
    }
}