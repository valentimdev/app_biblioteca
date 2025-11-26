package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.notifications.AppNotification
import java.text.SimpleDateFormat
import java.util.*

class NotificacoesAdapter(
    private var notificacoes: List<AppNotification>,
    private val onClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificacoesAdapter.ViewHolder>() {

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStatus: ImageView = itemView.findViewById(R.id.ivStatusIndicator)
        val tvAssunto: TextView = itemView.findViewById(R.id.tvAssunto)
        val tvData: TextView = itemView.findViewById(R.id.tvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacao, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notificacoes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notificacoes[position]

        holder.tvAssunto.text = notif.title
        holder.tvData.text = dateFmt.format(Date(notif.dateMillis))

        holder.ivStatus.visibility = if (notif.read) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onClick(notif)
        }
    }

    fun updateList(newList: List<AppNotification>) {
        this.notificacoes = newList
        notifyDataSetChanged()
    }
}
