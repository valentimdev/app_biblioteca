package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.databinding.ItemAdminEventoBinding

class AdminEventosAdapter(
    private var eventos: List<AdminEvento>,
    private val onSwitchChange: (String, AdminEvento) -> Unit
) : RecyclerView.Adapter<AdminEventosAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminEventoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminEventoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]
        with(holder.binding) {
            tvNomeEvento.text = evento.nome
            tvLocalEvento.text = "Local: ${evento.local}"
            tvVagasEvento.text = "Vagas: ${evento.vagas}"
            tvDataEvento.text = "Data: ${evento.data}"
            tvHorarioEvento.text = "Horário: ${evento.horario}"

            // remove listeners antigos pra não disparar ao reciclar
            switchAtivo.setOnCheckedChangeListener(null)
            switchInscricao.setOnCheckedChangeListener(null)

            switchAtivo.isChecked = evento.ativo
            switchInscricao.isChecked = evento.inscricaoAtiva

            switchAtivo.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
                onSwitchChange("Ativar/Desativar Evento", evento)
            }

            switchInscricao.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
                onSwitchChange("Permitir Inscrição", evento)
            }
        }
    }

    override fun getItemCount() = eventos.size

    // 👇 chama isso no fragment sempre que mudar a lista
    fun updateData(newList: List<AdminEvento>) {
        eventos = newList
        notifyDataSetChanged()
    }
}
