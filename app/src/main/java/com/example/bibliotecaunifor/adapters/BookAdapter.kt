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

    inner class ViewHolder(val b: ItemBookBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        with(holder.b) {
            txtTitle.text = book.nome
            txtAuthor.text = book.autor

            // Indicadores visuais
            val status = mutableListOf<String>()
            if (book.oculto) status.add("Oculto")
            if (!book.emprestimoHabilitado) status.add("Empréstimo desativado")

            txtStatus.apply {
                visibility = if (status.isEmpty()) View.GONE else View.VISIBLE
                text = status.joinToString(" • ")
            }

            ivMore.visibility = if (isAdmin) View.VISIBLE else View.GONE
            root.setOnClickListener { onAction("detail", book) }

            ivMore.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
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
    }

    override fun getItemCount() = books.size

    fun updateData(newList: List<Book>) {
        books = newList
        notifyDataSetChanged()
    }
}
