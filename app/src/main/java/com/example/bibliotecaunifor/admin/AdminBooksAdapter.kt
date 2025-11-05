package com.example.bibliotecaunifor.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R

class AdminBooksAdapter(
    private val books: MutableList<Book>,
    private val emprestimoState: MutableMap<String, Boolean>,
    private val visibilidadeState: MutableMap<String, Boolean>,
    private val onEdit: (Book) -> Unit,
    private val onRemove: (Book) -> Unit,
    private val onToggleEmprestimo: (Book) -> Unit,
    private val onToggleVisibilidade: (Book) -> Unit,
) : RecyclerView.Adapter<AdminBooksAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = books[position]
        holder.tvTitle.text = book.nome
        holder.tvAuthor.text = book.autor

        val visivel = visibilidadeState[book.id] ?: true
        val emprestimoOn = emprestimoState[book.id] ?: true

        // lógica do status vermelho
        when {
            !visivel -> {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Oculto"
            }
            visivel && !emprestimoOn -> {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Empréstimo desativado"
            }
            else -> {
                holder.tvStatus.visibility = View.GONE
            }
        }

        holder.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_book_admin, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> { onEdit(book); true }
                    R.id.action_remove -> { onRemove(book); true }
                    R.id.action_toggle_emprestimo -> { onToggleEmprestimo(book); true }
                    R.id.action_toggle_visibilidade -> { onToggleVisibilidade(book); true }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = books.size

    fun replaceAll(newList: List<Book>) {
        books.clear()
        books.addAll(newList)
        notifyDataSetChanged()
    }

    fun notifyBookChanged(id: String) {
        val idx = books.indexOfFirst { it.id == id }
        if (idx != -1) notifyItemChanged(idx)
    }

    fun remove(book: Book) {
        val idx = books.indexOfFirst { it.id == book.id }
        if (idx != -1) {
            books.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
