package com.example.bibliotecaunifor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.databinding.ItemAdminEventoBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminEventosAdapter(
    private val context: Context,
    private var eventosOriginais: List<AdminEvento>,
    private val onSwitchChange: (String, Boolean, AdminEvento) -> Unit,
    private val onItemClick: (AdminEvento) -> Unit
) : RecyclerView.Adapter<AdminEventosAdapter.ViewHolder>() {

    private var eventosFiltrados: List<AdminEvento> = eventosOriginais

    // Ajustado para datas com milissegundos
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

            // título
            tvNomeEvento.text = evento.title

            // local
            tvLocalEvento.text = context.getString(
                com.example.bibliotecaunifor.R.string.evento_local,
                evento.location
            )

            // vagas
            tvVagasEvento.text = context.getString(
                com.example.bibliotecaunifor.R.string.evento_vagas,
                evento.seats.toString()
            )

            // datas
            try {
                val date = parser.parse(evento.eventStartTime)
                val dataFormatada = dateFmt.format(date!!)
                val horaFormatada = timeFmt.format(date)

                tvDataEvento.text = context.getString(
                    com.example.bibliotecaunifor.R.string.evento_data,
                    dataFormatada
                )

                tvHorarioEvento.text = context.getString(
                    com.example.bibliotecaunifor.R.string.evento_horario,
                    horaFormatada
                )

            } catch (e: Exception) {
                tvDataEvento.text = context.getString(
                    com.example.bibliotecaunifor.R.string.evento_data,
                    evento.eventStartTime
                )
                tvHorarioEvento.text = context.getString(
                    com.example.bibliotecaunifor.R.string.evento_horario,
                    evento.eventEndTime ?: "-"
                )
            }

            // Switches
            switchAtivo.setOnCheckedChangeListener(null)
            switchInscricao.setOnCheckedChangeListener(null)

            // Ativo: ligado = evento ativo (isDisabled = false)
            switchAtivo.isChecked = !evento.isDisabled

            // Cálculo da inscrição ativa com base nas datas
            val agora = Date()
            val inicioInscricao = evento.registrationStartTime
                ?.takeIf { it.isNotBlank() && it != "null" }
                ?.let { runCatching { parser.parse(it) }.getOrNull() }

            val fimInscricao = evento.registrationEndTime
                ?.takeIf { it.isNotBlank() && it != "null" }
                ?.let { runCatching { parser.parse(it) }.getOrNull() }

            val inscricaoAtiva = if (inicioInscricao != null && fimInscricao != null) {
                agora.after(inicioInscricao) && agora.before(fimInscricao)
            } else {
                false
            }

            switchInscricao.isChecked = inscricaoAtiva

            // >>> AQUI ENTRA A CORREÇÃO: passar o isChecked (Boolean) <<<

            switchAtivo.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChange("Ativar/Desativar Evento", isChecked, evento)
            }

            switchInscricao.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChange("Permitir Inscrição", isChecked, evento)
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
            val nomeOk =
                query.isNullOrBlank() || evento.title.contains(query, ignoreCase = true)

            val dataOk = try {
                val dataEvento = parser.parse(evento.eventStartTime)
                when (tipoFiltro) {
                    "proximos" -> dataEvento != null && !dataEvento.before(agora)
                    "distantes" -> dataEvento != null && dataEvento.before(agora)
                    else -> true
                }
            } catch (_: Exception) {
                true
            }

            nomeOk && dataOk
        }

        eventosFiltrados = when (tipoFiltro) {
            "proximos" -> eventosFiltrados.sortedBy { parser.parse(it.eventStartTime) }
            "distantes" -> eventosFiltrados.sortedByDescending { parser.parse(it.eventStartTime) }
            else -> eventosFiltrados
        }

        notifyDataSetChanged()
    }

    fun limparFiltros() {
        eventosFiltrados = eventosOriginais
        notifyDataSetChanged()
    }
}
