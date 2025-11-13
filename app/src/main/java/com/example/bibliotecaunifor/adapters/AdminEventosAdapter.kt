package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.databinding.ItemAdminEventoBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminEventosAdapter(
    private var eventosOriginais: List<AdminEvento>,
    private val onSwitchChange: (String, AdminEvento) -> Unit,
    private val onItemClick: (AdminEvento) -> Unit
) : RecyclerView.Adapter<AdminEventosAdapter.ViewHolder>() {

    private var eventosFiltrados: List<AdminEvento> = eventosOriginais

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFmt = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

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
        val evento = eventosFiltrados[position]
        with(holder.binding) {
            tvNomeEvento.text = evento.nome
            tvLocalEvento.text = "Local: ${evento.local}"
            tvVagasEvento.text = "Vagas: ${evento.vagas}"

            try {
                val date = parser.parse(evento.data)
                tvDataEvento.text = "Data: ${dateFmt.format(date!!)}"
                tvHorarioEvento.text = "Horário: ${timeFmt.format(date)}"
            } catch (e: Exception) {
                tvDataEvento.text = "Data: ${evento.data}"
                tvHorarioEvento.text = "Horário: ${evento.horario}"
            }

            switchAtivo.setOnCheckedChangeListener(null)
            switchInscricao.setOnCheckedChangeListener(null)

            switchAtivo.isChecked = evento.ativo

            switchAtivo.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
                onSwitchChange("Ativar/Desativar Evento", evento)
            }

            switchInscricao.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
                onSwitchChange("Permitir Inscrição", evento)
            }

            root.setOnClickListener { onItemClick(evento) }
        }
    }

    override fun getItemCount() = eventosFiltrados.size

    fun updateData(newList: List<AdminEvento>) {
        eventosOriginais = newList
        eventosFiltrados = newList
        notifyDataSetChanged()
    }

    fun filtrarEventos(query: String?, tipoFiltro: String?) {
        val agora = Date()

        eventosFiltrados = eventosOriginais.filter { evento ->
            val nomeOk = query.isNullOrBlank() || evento.nome.contains(query, ignoreCase = true)

            val dataOk = try {
                val dataEvento = parser.parse(evento.data)
                when (tipoFiltro) {
                    "proximos" -> dataEvento != null && !dataEvento.before(agora)
                    "distantes" -> dataEvento != null && dataEvento.before(agora)
                    else -> true
                }
            } catch (e: Exception) {
                true
            }

            nomeOk && dataOk
        }

        eventosFiltrados = when (tipoFiltro) {
            "proximos" -> eventosFiltrados.sortedBy { parser.parse(it.data) }
            "distantes" -> eventosFiltrados.sortedByDescending { parser.parse(it.data) }
            else -> eventosFiltrados
        }

        notifyDataSetChanged()
    }

    fun limparFiltros() {
        eventosFiltrados = eventosOriginais
        notifyDataSetChanged()
    }
}
