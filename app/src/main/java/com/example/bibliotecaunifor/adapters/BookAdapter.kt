package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        b.tvTitle.text = book.title
        b.tvAuthor.text = book.author

        Glide.with(b.root.context)
            .load(book.imageUrl ?: "")
            .placeholder(R.drawable.placeholder_user)
            .error(R.drawable.placeholder_book)
            .into(b.imgThumb)

        b.btnMore.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Status de empréstimo ativo
        if (book.isRentedByUser) {
            b.tvStatus.visibility = View.VISIBLE
            b.tvStatus.text = "Você já possui empréstimo com esse livro"
            b.tvStatus.setTextColor(b.root.context.getColor(android.R.color.holo_red_dark))
        } else {
            b.tvStatus.visibility = View.GONE
        }

        b.root.setOnClickListener { onAction("detail", book) }

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