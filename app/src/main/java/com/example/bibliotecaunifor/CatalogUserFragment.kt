package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.ui.book.BookDetailFragment
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.bibliotecaunifor.R

class CatalogUserFragment : Fragment(R.layout.fragment_catalog) {

    private lateinit var rvBooks: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var adapter: BookAdapter
    private val allBooks = mutableListOf<Book>()
    private var showOnlyAvailable = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvBooks = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookAdapter(allBooks, false) { action, book ->
            if (action == "detail") {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, false))
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

        fetchBooks()
    }

    private fun fetchBooks() {
        val tokenStr = AuthUtils.getToken(requireContext())
        if (tokenStr.isNullOrEmpty()) {
            android.util.Log.e("CatalogUserFragment", "Token nulo!")
            return
        }
        val token = "Bearer $tokenStr"

        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                android.util.Log.d("CatalogUserFragment", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    applyFilterAndSearch()
                } else {
                    android.util.Log.e(
                        "CatalogUserFragment",
                        "Erro ao buscar livros: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                android.util.Log.e("CatalogUserFragment", "Falha na requisição", t)
            }
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

        if (showOnlyAvailable) {
            list = list.filter { it.availableCopies > 0 }
        }

        adapter.updateData(list.toList())
    }
}
