// com/example/bibliotecaunifor/adapters/RecommendationsAdapter.kt
package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R

class RecommendationsAdapter(
    private var books: List<Book>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa: ImageView = itemView.findViewById(R.id.imgCapaRecommendation)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = books[position]
        holder.tvTitle.text = book.title

        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.placeholder_book) // crie esse drawable
            .error(R.drawable.placeholder_book)
            .into(holder.imgCapa)

        holder.itemView.setOnClickListener {
            onClick(book.id)
        }
    }

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
