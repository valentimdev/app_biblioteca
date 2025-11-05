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
import com.example.bibliotecaunifor.admin.AdminActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val sample = mutableListOf(
        Book("1", "Clean Code", "Robert C. Martin"),
        Book("2", "Kotlin in Action", "D. Jemerov, S. Isakova"),
        Book("3", "Effective Java", "Joshua Bloch", oculto = true),
        Book("4", "Design Patterns", "GoF", emprestimoHabilitado = false)
    )

    private lateinit var adapter: BookAdapter
    private var isAdmin = true // <- depois você pode setar isso conforme login

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookAdapter(sample, isAdmin) { action, book ->
            when (action) {
                "detail" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, isAdmin))
                    .addToBackStack(null)
                    .commit()

                "edit" -> abrirDialogLivro(book)
                "remove" -> removerLivro(book)

                "toggleEmprestimo" -> {
                    book.emprestimoHabilitado = !book.emprestimoHabilitado
                    adapter.notifyDataSetChanged()
                }

                "toggleVisibilidade" -> {
                    book.oculto = !book.oculto
                    adapter.notifyDataSetChanged()
                }
            }
        }

        rv.adapter = adapter

        // botão de filtro (abre o mesmo dialog multiopção que no admin eventos)
        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { abrirDialogFiltros() }

        // busca
        val edt = view.findViewById<EditText>(R.id.edtSearch)
        edt.doAfterTextChanged { text ->
            val filtered = sample.filter {
                it.nome.contains(text.toString(), ignoreCase = true)
                        || it.autor.contains(text.toString(), ignoreCase = true)
            }
            adapter.updateData(filtered)
        }

        // botão de adicionar no header (configurado pela MainActivity)

    }

    private fun abrirDialogFiltros() {
        val items = arrayOf("Apenas visíveis", "Somente ocultos", "Com empréstimo ativo", "Com empréstimo desativado")
        val checked = booleanArrayOf(false, false, false, false)

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar livros")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Aplicar") { _, _ ->
                var filtrados = sample.toList()
                if (checked[0]) filtrados = filtrados.filter { !it.oculto }
                if (checked[1]) filtrados = filtrados.filter { it.oculto }
                if (checked[2]) filtrados = filtrados.filter { it.emprestimoHabilitado }
                if (checked[3]) filtrados = filtrados.filter { !it.emprestimoHabilitado }
                adapter.updateData(filtrados)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirDialogLivro(book: Book? = null) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_book, null)

        val inputNome = view.findViewById<TextInputEditText>(R.id.edtNome)
        val inputAutor = view.findViewById<TextInputEditText>(R.id.edtAutor)

        if (book != null) {
            inputNome.setText(book.nome)
            inputAutor.setText(book.autor)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (book == null) "Adicionar livro" else "Editar livro")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = inputNome.text.toString().trim()
                val autor = inputAutor.text.toString().trim()
                if (nome.isEmpty() || autor.isEmpty()) {
                    Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (book == null) {
                    val novo = Book((sample.size + 1).toString(), nome, autor)
                    sample.add(novo)
                } else {
                    book.nome = nome
                    book.autor = autor
                }

                adapter.updateData(sample)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removerLivro(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remover livro")
            .setMessage("Deseja remover '${book.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                sample.remove(book)
                adapter.updateData(sample)
            }
            .setNegativeButton("Não", null)
            .show()
    }
}

