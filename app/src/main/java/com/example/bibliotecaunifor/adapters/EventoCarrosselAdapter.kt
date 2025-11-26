package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.R

class EventoCarrosselAdapter(
    private val eventos: List<Evento>,
    private val onClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoCarrosselAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloEventoCarrossel)
        val imgEvento: ImageView = itemView.findViewById(R.id.imgEventoCarrossel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_carrossel, parent, false)
        return EventoViewHolder(view)
    }

    override fun getItemCount() = eventos.size

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        // Título
        holder.tvTitulo.text = evento.title

        // Imagem do evento
        Glide.with(holder.itemView.context)
            .load(evento.imageUrl)
            .placeholder(R.drawable.placeholder_evento) // coloca um drawable seu aqui
            .error(R.drawable.placeholder_evento)
            .centerCrop()
            .into(holder.imgEvento)

        // Clique no card
        holder.itemView.setOnClickListener { onClick(evento) }
    }
}
