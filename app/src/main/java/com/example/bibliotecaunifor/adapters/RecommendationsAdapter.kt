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
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
        holder.itemView.setOnClickListener {
            onBookClick(book)   // 👈 passa o Book inteiro pro callback
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(book: Book) {
            tvTitle.text = book.title

            Glide.with(itemView.context)
                .load(book.imageUrl)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.placeholder_book)
                .centerCrop()
                .into(imgCover)
        }
    }
}
