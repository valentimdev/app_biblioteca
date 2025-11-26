package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R

class LivroAdapter(
    private var itens: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

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

        holder.text.text = livro.title

        Glide.with(holder.itemView.context)
            .load(livro.imageUrl)
            .placeholder(R.drawable.placeholder_book) // coloque um drawable
            .error(R.drawable.placeholder_book)
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener { onClick(livro) }
    }

    override fun getItemCount(): Int = itens.size

    fun updateList(novosLivros: List<Book>) {
        itens = novosLivros
        notifyDataSetChanged()
    }
}
