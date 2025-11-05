package com.example.bibliotecaunifor.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.BookDetailFragment
import com.example.bibliotecaunifor.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CatalogAdminFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var edtSearch: EditText
    private var currentFilter: FilterType = FilterType.TODOS

    // mock de livros
    private val allBooks = mutableListOf(
        Book("1", "A Metamorfose", "Franz Kafka"),
        Book("2", "1984", "George Orwell", oculto = true),
        Book("3", "A Revolução dos Bichos", "George Orwell", emprestimoHabilitado = false),
        Book("4", "O Processo", "Franz Kafka"),
    )

    // tipos de filtro
    enum class FilterType { TODOS, VISIVEIS, OCULTOS, EMPRESTIMO_DESATIVADO }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvBooks = view.findViewById(R.id.rvBooks)
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        edtSearch = view.findViewById(R.id.edtSearch)

        val btnFilter = view.findViewById<View>(R.id.btnFilter)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)

        // mostra botão de adicionar no admin
        btnAdd.visibility = View.VISIBLE
        btnAdd.setOnClickListener { showAddDialog() }

        // abre o dialog de filtros
        btnFilter.setOnClickListener { showFilterDialog() }

        // busca por texto
        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }

        // inicializa adapter
        adapter = BookAdapter(allBooks, true) { action, book ->
            when (action) {
                "detail" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.admin_container, BookDetailFragment.newInstance(book, true))
                    .addToBackStack(null)
                    .commit()
                "edit" -> showAddDialog(book)
                "remove" -> {
                    allBooks.remove(book)
                    applyFilterAndSearch()
                }
                "toggleEmprestimo" -> {
                    book.emprestimoHabilitado = !book.emprestimoHabilitado
                    applyFilterAndSearch()
                }
                "toggleVisibilidade" -> {
                    book.oculto = !book.oculto
                    applyFilterAndSearch()
                }
            }
        }

        rvBooks.adapter = adapter
    }

    // dialog para escolher tipo de filtro
    private fun showFilterDialog() {
        val options = arrayOf("Todos", "Visíveis", "Ocultos", "Empréstimo desativado")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar livros")
            .setItems(options) { _, which ->
                currentFilter = when (which) {
                    1 -> FilterType.VISIVEIS
                    2 -> FilterType.OCULTOS
                    3 -> FilterType.EMPRESTIMO_DESATIVADO
                    else -> FilterType.TODOS
                }
                applyFilterAndSearch()
            }
            .show()
    }

    // aplica busca + filtro e atualiza a lista
    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        var list = allBooks.asSequence()

        list = when (currentFilter) {
            FilterType.TODOS -> list
            FilterType.VISIVEIS -> list.filter { !it.oculto }
            FilterType.OCULTOS -> list.filter { it.oculto }
            FilterType.EMPRESTIMO_DESATIVADO -> list.filter { !it.emprestimoHabilitado }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.nome.contains(query, true) || it.autor.contains(query, true)
            }
        }

        adapter.updateData(list.toList())
    }

    // mock do dialog de adicionar/editar livro
    private fun showAddDialog(book: Book? = null) {
        val layout = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val edtNome = layout.findViewById<EditText>(R.id.edtNome)
        val edtAutor = layout.findViewById<EditText>(R.id.edtAutor)

        if (book != null) {
            edtNome.setText(book.nome)
            edtAutor.setText(book.autor)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (book == null) "Adicionar Livro" else "Editar Livro")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = edtNome.text.toString()
                val autor = edtAutor.text.toString()
                if (book == null) {
                    allBooks.add(Book((allBooks.size + 1).toString(), nome, autor))
                } else {
                    book.nome = nome
                    book.autor = autor
                }
                applyFilterAndSearch()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
