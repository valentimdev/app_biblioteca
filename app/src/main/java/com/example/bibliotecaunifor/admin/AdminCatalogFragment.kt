package com.example.bibliotecaunifor.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter

class AdminCatalogFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var btnFilter: ImageButton

    // lista base (mock)
    private val allBooks = mutableListOf(
        Book("1", "MERIDIANO DE SANGUE", "Cormac McCarthy"),
        Book("2", "Authenticgames: vivendo uma vida autêntica", "AuthenticGames"),
        Book("3", "Mourinho (the special one)", "Diego Torres"),
        Book("4", "O Caçador de Pipas", "Khaled Hosseini"),
    )

    // 👇 AGORA o adapter é do tipo BookAdapter
    private lateinit var adapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        btnFilter = view.findViewById(R.id.btnFilter)

        adapter = BookAdapter(
            books = allBooks,
            isAdmin = true
        ) { action, book ->
            when (action) {
                "edit" -> showAddOrEditDialog(book)
                "remove" -> removeBook(book)
                "toggleEmprestimo" -> toggleEmprestimo(book)
                "toggleVisibilidade" -> toggleVisibilidade(book)
                "detail" -> {
                    // aqui você pode abrir um fragment de detalhe,
                    // mas por enquanto vamos só deixar assim
                }
            }
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // busca simples
        edtSearch.setOnEditorActionListener { _, _, _ ->
            filterList(edtSearch.text.toString())
            true
        }

        btnFilter.setOnClickListener {
            // futuro filtro
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                showAddOrEditDialog(null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterList(query: String) {
        if (query.isBlank()) {
            adapter.updateData(allBooks)
        } else {
            val filtered = allBooks.filter {
                it.nome.contains(query, true) || it.autor.contains(query, true)
            }
            adapter.updateData(filtered)
        }
    }

    private fun showAddOrEditDialog(book: Book?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val edtNome = dialogView.findViewById<EditText>(R.id.edtNome)
        val edtAutor = dialogView.findViewById<EditText>(R.id.edtAutor)

        if (book != null) {
            edtNome.setText(book.nome)
            edtAutor.setText(book.autor)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (book == null) "Adicionar livro" else "Editar livro")
            .setView(dialogView)
            .setPositiveButton("Salvar") { d, _ ->
                val nome = edtNome.text.toString().trim()
                val autor = edtAutor.text.toString().trim()
                if (nome.isNotEmpty()) {
                    if (book == null) {
                        val novo = Book(
                            id = System.currentTimeMillis().toString(),
                            nome = nome,
                            autor = autor
                        )
                        allBooks.add(0, novo)
                    } else {
                        book.nome = nome
                        book.autor = autor
                    }
                    filterList(edtSearch.text.toString())
                }
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removeBook(book: Book) {
        allBooks.removeAll { it.id == book.id }
        filterList(edtSearch.text.toString())
    }

    private fun toggleEmprestimo(book: Book) {
        book.emprestimoHabilitado = !book.emprestimoHabilitado
        filterList(edtSearch.text.toString())
    }

    private fun toggleVisibilidade(book: Book) {
        book.oculto = !book.oculto
        filterList(edtSearch.text.toString())
    }
}
