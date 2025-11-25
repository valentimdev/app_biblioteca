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
            tvNome.text = u.name
            tvMatricula.text = "Matrícula: ${u.matricula}"

            if (u.status == UserStatus.BANNED) {
                chipStatus.text = "BLOQUEADO"
                chipStatus.setTextColor(
                    ContextCompat.getColor(root.context, R.color.error)
                )
            } else {
                chipStatus.text = "ATIVO"
                chipStatus.setTextColor(
                    ContextCompat.getColor(root.context, R.color.teal_700)
                )
            }

            root.setOnClickListener { onClick(u) }
        }
    }
}
