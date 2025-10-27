// app/src/main/java/com/example/bibliotecaunifor/admin/UsersAdapter.kt
package com.example.bibliotecaunifor.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.databinding.ItemUserAdminBinding

class UsersAdapter(
    private val onClick: (User) -> Unit
) : ListAdapter<User, UsersAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }

    inner class VH(val b: ItemUserAdminBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemUserAdminBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = getItem(position)
        with(holder.b) {
            tvNome.text = u.nome
            tvMatricula.text = "Matrícula: ${u.matricula}"
            chipStatus.text = u.status.name

            // Define a cor do texto de acordo com o status
            val color = if (u.status == UserStatus.ATIVO) {
                // Use uma cor existente no seu projeto; ajuste se não tiver teal_700
                ContextCompat.getColor(root.context, R.color.teal_700)
            } else {
                // Se não existir R.color.error, troque por R.color.red ou similar
                ContextCompat.getColor(root.context, R.color.error)
            }
            chipStatus.setTextColor(color)

            // Clique no card
            root.setOnClickListener { onClick(u) }
        }
    }
}
