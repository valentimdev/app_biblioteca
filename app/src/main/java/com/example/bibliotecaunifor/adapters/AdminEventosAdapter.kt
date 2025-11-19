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

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
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
            tvNomeEvento.text = evento.title
            tvLocalEvento.text = "Local: ${evento.location}"
            tvVagasEvento.text = "Vagas: ${evento.seats}"

            try {
                val date = parser.parse(evento.startTime)
                tvDataEvento.text = "Data: ${dateFmt.format(date!!)}"
                tvHorarioEvento.text = "Horário: ${timeFmt.format(date)}"
            } catch (e: Exception) {
                tvDataEvento.text = "Data: ${evento.startTime}"
                tvHorarioEvento.text = "Horário: ${evento.endTime ?: evento.startTime}"
            }

            switchAtivo.setOnCheckedChangeListener(null)
            switchInscricao.setOnCheckedChangeListener(null)

            switchAtivo.isChecked = !evento.isDisabled

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
            val nomeOk = query.isNullOrBlank() || evento.title.contains(query, ignoreCase = true)

            val dataOk = try {
                val dataEvento = parser.parse(evento.startTime)
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
            "proximos" -> eventosFiltrados.sortedBy { parser.parse(it.startTime) }
            "distantes" -> eventosFiltrados.sortedByDescending { parser.parse(it.startTime) }
            else -> eventosFiltrados
        }

        notifyDataSetChanged()
    }

    fun limparFiltros() {
        eventosFiltrados = eventosOriginais
        notifyDataSetChanged()
    }
}
