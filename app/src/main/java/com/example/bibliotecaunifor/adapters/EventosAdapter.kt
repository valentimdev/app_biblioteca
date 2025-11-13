package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.R

class EventosAdapter(
    private val eventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit // callback para clique
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.titulo.text = evento.title

        holder.itemView.setOnClickListener {
            onItemClick(evento)
        }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.textTituloEvento)
    }
}