package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.UserResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogUserFragment : Fragment(R.layout.fragment_catalog) {

    private lateinit var rvBooks: androidx.recyclerview.widget.RecyclerView
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
                    .add(
                        R.id.fragment_container,
                        BookDetailFragment.newInstance(
                            book,
                            userRentedBookIds.contains(book.id)
                        ),
                        "book_detail"
                    )
                    .hide(this)
                    .addToBackStack("book_detail")
                    .commit()
            }
        }

        rvBooks.adapter = adapter

        btnFilter.setOnClickListener { showFilterDialog() }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }

        loadUserRentalsAndBooks()
    }

    private fun loadUserRentalsAndBooks() {
        RetrofitClient.userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val rentals = response.body()!!.rentals
                    userRentedBookIds = rentals
                        .filter { it.returnDate == null }
                        .mapNotNull { it.bookId }

                    fetchBooks()
                } else {
                    userRentedBookIds = emptyList()
                    fetchBooks()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("CatalogUser", "Erro ao carregar usuário", t)
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
                Log.e("CatalogUser", "Erro ao carregar livros", t)
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
        val query = edtSearch.text.toString().trim().lowercase()
        var filtered = allBooks.asSequence()

        if (query.isNotBlank()) {
            filtered = filtered.filter { book ->
                book.title.lowercase().contains(query) ||
                        book.author.lowercase().contains(query) ||
                        (book.isbn?.lowercase()?.contains(query) == true)
            }
        }

        if (showOnlyAvailable) {
            filtered = filtered.filter { it.availableCopies > 0 }
        }

        adapter.updateData(filtered.toList())
    }

    fun refreshCatalog() {
        loadUserRentalsAndBooks()
    }
}
