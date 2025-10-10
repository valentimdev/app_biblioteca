package com.example.bibliotecaunifor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecommendationAdapter(
    private val items: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val title: TextView = v.findViewById(R.id.txtTitle)
        init { v.setOnClickListener { onClick(items[bindingAdapterPosition]) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recomendation, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(h: VH, pos: Int) {
        val b = items[pos]
        h.title.text = b.title
        b.coverRes?.let { h.img.setImageResource(it) }
    }
    override fun getItemCount() = items.size
}
