package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.model.Book

class BookAdapter(
    private var data: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvYearQty: TextView = itemView.findViewById(R.id.tvYearQty)
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH{
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val b = data[position]
        h.tvTitle.text = b.title
        h.tvAuthor.text = "${b.author} • ${b.genre}"
        h.tvYearQty.text = "${b.year} • Quantidade: ${b.quantity}"
        h.itemView.setOnClickListener { onClick(b) }
        // imagem mock: h.imgCover.setImageResource(R.drawable.ic_menu_book)
    }

    override fun getItemCount() = data.size

    fun filter(query: String) {

    }
}
