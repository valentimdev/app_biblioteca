package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemBookBinding

class BookAdapter(
    private var books: List<Book>,
    private val isAdmin: Boolean,
    private val onAction: (String, Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        val b = holder.binding

        // dados principais
        b.tvTitle.text = book.nome
        b.tvAuthor.text = book.autor

        // status (oculto / empréstimo desativado)
        val status = mutableListOf<String>()
        if (book.oculto) status.add("Oculto")
        if (!book.emprestimoHabilitado) status.add("Empréstimo desativado")

        if (status.isEmpty()) {
            b.tvStatus.visibility = View.GONE
        } else {
            b.tvStatus.visibility = View.VISIBLE
            b.tvStatus.text = status.joinToString(" • ")
        }

        // mostra ou esconde o botão de opções conforme for admin
        b.btnMore.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // clique no card -> detalhe
        b.root.setOnClickListener { onAction("detail", book) }

        // clique no botão -> menu admin
        b.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_book_admin, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> onAction("edit", book)
                    R.id.action_remove -> onAction("remove", book)
                    R.id.action_toggle_emprestimo -> onAction("toggleEmprestimo", book)
                    R.id.action_toggle_visibilidade -> onAction("toggleVisibilidade", book)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateData(newList: List<Book>) {
        books = newList
        notifyDataSetChanged()
    }
}
