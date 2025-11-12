package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog

class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private var isAdmin: Boolean = false

    companion object {
        fun newInstance(book: Book, isAdmin: Boolean = false): BookDetailFragment {
            val frag = BookDetailFragment()
            val args = Bundle().apply {
                putString("id", book.id)
                putString("title", book.title)
                putString("author", book.author)
                putString("isbn", book.isbn ?: "")
                putString("description", book.description ?: "")
                putInt("totalCopies", book.totalCopies ?: 0)
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
                title = it.getString("title") ?: "",
                author = it.getString("author") ?: "",
                isbn = it.getString("isbn") ?: "",
                description = it.getString("description"),
                totalCopies = it.getInt("totalCopies"),
                createdAt = it.getString("createdAt") ?: "",
                updatedAt = it.getString("updatedAt") ?: "",
                availableCopies = it.getInt("availableCopies"),
                adminId = it.getString("adminId") ?: ""
            )
            isAdmin = it.getBoolean("isAdmin", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_book_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtAuthor = view.findViewById<TextView>(R.id.txtAuthor)
        val txtIsbn = view.findViewById<TextView>(R.id.txtIsbn)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val txtTotalCopies = view.findViewById<TextView>(R.id.txtTotalCopies)
        val btnAction = view.findViewById<Button>(R.id.btnAction)

        txtTitle.text = book.title
        txtAuthor.text = "Autor: ${book.author}"
        txtIsbn.text = "ISBN: ${book.isbn ?: "-"}"
        txtDescription.text = "Descrição: ${book.description ?: "-"}"
        txtTotalCopies.text = "Cópias: ${book.totalCopies ?: 0}"

        if (isAdmin) {
            btnAction.text = "Gerenciar estoque"
            btnAction.setOnClickListener { showAdminInfo() }
        } else {
            btnAction.setOnClickListener { showUserRentInfo() }
        }
    }

    private fun showAdminInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Estoque do livro")
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun showUserRentInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Solicitação de empréstimo")
            .setMessage("Você deseja alugar o livro \"${book.title}\"?")
            .setPositiveButton("Sim") { d, _ ->
                d.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle("Empréstimo confirmado")
                    .setMessage("Seu pedido foi registrado. Retire o livro na biblioteca.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
