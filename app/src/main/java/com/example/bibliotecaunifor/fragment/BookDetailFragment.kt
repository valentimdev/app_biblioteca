package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class BookDetailFragment : Fragment() {

    private lateinit var book: Book
    private var isAdmin: Boolean = false

    companion object {
        fun newInstance(book: Book, isAdmin: Boolean = false): BookDetailFragment {
            val frag = BookDetailFragment()
            val args = Bundle().apply {
                putString("id", book.id)
                putString("nome", book.nome)
                putString("autor", book.autor)
                putBoolean("oculto", book.oculto)
                putBoolean("emprestimoHabilitado", book.emprestimoHabilitado)
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
                nome = it.getString("nome") ?: "",
                autor = it.getString("autor") ?: "",
                oculto = it.getBoolean("oculto"),
                emprestimoHabilitado = it.getBoolean("emprestimoHabilitado")
            )
            isAdmin = it.getBoolean("isAdmin", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtAuthor = view.findViewById<TextView>(R.id.txtAuthor)
        val txtId = view.findViewById<TextView>(R.id.txtId)
        val btnAction = view.findViewById<Button>(R.id.btnAction)

        txtTitle.text = book.nome
        txtAuthor.text = "Autor: ${book.autor}"
        txtId.text = "ID: ${book.id}"

        if (isAdmin) {
            btnAction.text = "Gerenciar estoque"
            btnAction.setOnClickListener { showAdminInfo() }
        } else {
            // usuário
            btnAction.text = if (book.emprestimoHabilitado) "Alugar livro" else "Indisponível"
            btnAction.isEnabled = book.emprestimoHabilitado
            btnAction.setOnClickListener { showUserRentInfo() }
        }
    }

    private fun showAdminInfo() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Estoque do livro")
            .setMessage("Quantidade disponível: 12\nEmpréstimos ativos: 4")
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun showUserRentInfo() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Solicitação de empréstimo")
            .setMessage("Você deseja alugar o livro \"${book.nome}\"?")
            .setPositiveButton("Sim") { d, _ ->
                d.dismiss()
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Empréstimo confirmado")
                    .setMessage("Seu pedido foi registrado. Retire o livro na biblioteca.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
