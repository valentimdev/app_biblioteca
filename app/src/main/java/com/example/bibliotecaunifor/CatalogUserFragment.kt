package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.BookDetailFragment
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CatalogUserFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var adapter: BookAdapter
    private var showOnlyAvailable = false

    // mock
    private val allBooks = mutableListOf(
        Book("1", "Clean Code", "Robert C. Martin"),
        Book("2", "Kotlin in Action", "D. Jemerov, S. Isakova"),
        Book("3", "Effective Java", "Joshua Bloch", oculto = true),          // não deve aparecer
        Book("4", "Design Patterns", "GoF", emprestimoHabilitado = false),   // não deve aparecer
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvBooks = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)

        // user não adiciona livro
        btnAdd.visibility = View.GONE

        rvBooks.layoutManager = LinearLayoutManager(requireContext())

        // 👇 AQUI: isAdmin = false -> esconde o lápis
        adapter = BookAdapter(allBooks, false) { action, book ->
            if (action == "detail") {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, false))
                    .addToBackStack(null)
                    .commit()
            }
        }
        rvBooks.adapter = adapter

        // botão de filtro
        btnFilter.setOnClickListener { showFilterDialog() }

        // busca
        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }

        // 👇 importantíssimo: aplica o filtro logo que a tela abre
        applyFilterAndSearch()
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
        val search = edtSearch.text.toString().trim()

        // começa SEMPRE escondendo os ocultos
        var list = allBooks.asSequence()
            .filter { !it.oculto }

        // se selecionou "disponíveis", esconde os com empréstimo desativado
        if (showOnlyAvailable) {
            list = list.filter { it.emprestimoHabilitado }
        }

        // busca por texto
        if (search.isNotBlank()) {
            list = list.filter {
                it.nome.contains(search, ignoreCase = true) ||
                        it.autor.contains(search, ignoreCase = true)
            }
        }

        adapter.updateData(list.toList())
    }
}
