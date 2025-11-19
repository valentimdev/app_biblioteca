package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.BookWithRentalStatus
import com.example.bibliotecaunifor.R

class LivroWithRentalAdapter(
    private val itens: List<BookWithRentalStatus>,
    private val onClick: (BookWithRentalStatus) -> Unit
) : RecyclerView.Adapter<LivroWithRentalAdapter.LivroViewHolder>() {

    inner class LivroViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageItem)
        val text: TextView = itemView.findViewById(R.id.textItemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_carrossel, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = itens[position]
        holder.text.text = livro.book.title

        livro.book.imageUrl?.let {
            Glide.with(holder.image.context)
                .load(it)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.image)
        }

        holder.itemView.setOnClickListener { onClick(livro) }
    }

    override fun getItemCount(): Int = itens.size
}
