package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R

class EventoAdapter(
    private val itens: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageItem)
        val text: TextView = itemView.findViewById(R.id.textItemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_carrossel, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = itens[position]
        holder.text.text = evento
        holder.image.setImageResource(R.drawable.ic_launcher_foreground)
        holder.itemView.setOnClickListener { onClick(evento) }
    }

    override fun getItemCount(): Int = itens.size
}
