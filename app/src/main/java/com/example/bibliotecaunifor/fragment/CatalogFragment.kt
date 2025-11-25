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
import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.utils.AuthUtils
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
        rvBooks = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookAdapter(allBooks, false) { action, book ->
            if (action == "detail") {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, userRentedBookIds.contains(book.id)))
                    .commit()
            }
        }
        rvBooks.adapter = adapter

        btnFilter.setOnClickListener { showFilterDialog() }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }

        fetchUserRentals { rentedIds ->
            userRentedBookIds = rentedIds
            fetchBooks()
        }
    }

    private fun fetchUserRentals(callback: (List<String>) -> Unit) {
        val token = AuthUtils.getToken(requireContext()) ?: return callback(emptyList())
        val bearer = "Bearer $token"

        RetrofitClient.bookApi.getMyRentals(bearer).enqueue(object : Callback<List<Rental>> {
            override fun onResponse(call: Call<List<Rental>>, response: Response<List<Rental>>) {
                val activeBookIds = response.body()?.filter { it.returnDate == null }?.map { it.bookId } ?: emptyList()
                callback(activeBookIds)
            }

            override fun onFailure(call: Call<List<Rental>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    private fun fetchBooks() {
        val tokenStr = AuthUtils.getToken(requireContext()) ?: return
        val token = "Bearer $tokenStr"

        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    val books = response.body() ?: emptyList()
                    books.forEach { it.isRentedByUser = userRentedBookIds.contains(it.id) }
                    allBooks.addAll(books)
                    applyFilterAndSearch()
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun showFilterDialog() {
        val opts = arrayOf("Todos", "Disponíveis para alugar")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar")
            .setItems(opts) { _, which ->
                showOnlyAvailable = (which == 1)
                applyFilterAndSearch()
            }
            .show()
    }

    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        var list = allBooks.asSequence()

        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true)
            }
        }

        if (showOnlyAvailable) list = list.filter { it.availableCopies > 0 }

        adapter.updateData(list.toList())
    }
}
