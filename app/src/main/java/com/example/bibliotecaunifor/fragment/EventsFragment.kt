package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.EventosAdapter
import com.example.bibliotecaunifor.api.EventApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class EventsFragment : Fragment(R.layout.fragment_events) {

    private lateinit var recyclerViewEventos: androidx.recyclerview.widget.RecyclerView
    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView

    private var todosEventos: List<Evento> = listOf()
    private var diaSelecionado: Calendar? = null

    // O backend envia formato ISO UTC (ex: 2025-11-11T19:00:00.000Z)
    private val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val formatoExibicao = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewEventos = view.findViewById(R.id.recyclerViewEventos)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())

        carregarEventos()

        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val dataSelecionada = eventDay.calendar
                if (diaSelecionado != null && ehMesmoDia(diaSelecionado!!, dataSelecionada)) {
                    diaSelecionado = null
                    atualizarEventosNoCalendario()
                    mostrarEventos(todosEventos)
                } else {
                    diaSelecionado = dataSelecionada
                    atualizarEventosNoCalendario()
                    filtrarEventosPorDia(dataSelecionada)
                }
            }
        })
    }

    private fun carregarEventos() {
        Thread {
            try {
                val eventosApi = EventApi.fetchEventos()
                val eventosConvertidos = eventosApi.map { e ->
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = formatoISO.parse(e.startTime)
                    } catch (_: Exception) { }
                    e // já é um Evento no novo formato
                }

                requireActivity().runOnUiThread {
                    todosEventos = eventosConvertidos
                    atualizarEventosNoCalendario()
                    mostrarEventos(todosEventos)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    private fun atualizarEventosNoCalendario() {
        val eventosMarcados = todosEventos.mapNotNull { evento ->
            try {
                val cal = Calendar.getInstance()
                cal.time = formatoISO.parse(evento.startTime)!!
                EventDay(cal, R.drawable.ic_event_marker)
            } catch (_: Exception) {
                null
            }
        }.toMutableList()

        diaSelecionado?.let {
            eventosMarcados.add(EventDay(it, R.drawable.ic_day_selected))
        }

        calendarView.setEvents(eventosMarcados)
    }

    private fun ehMesmoDia(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }

    private fun filtrarEventosPorDia(dataSelecionada: Calendar) {
        val eventosDoDia = todosEventos.filter { evento ->
            try {
                val calEvento = Calendar.getInstance()
                calEvento.time = formatoISO.parse(evento.startTime)!!
                ehMesmoDia(calEvento, dataSelecionada)
            } catch (_: Exception) {
                false
            }
        }
        mostrarEventos(eventosDoDia)
    }

    private fun mostrarEventos(eventos: List<Evento>) {
        recyclerViewEventos.adapter = EventosAdapter(eventos) { evento ->
            mostrarDetalhesEvento(evento)
        }
    }

    private fun mostrarDetalhesEvento(evento: Evento) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_info_evento, null)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloEvento)
        val tvDescricao = view.findViewById<TextView>(R.id.tvDescricaoEvento)
        val tvDataHora = view.findViewById<TextView>(R.id.tvDataHoraEvento)
        val btnFechar = view.findViewById<Button>(R.id.buttonFecharEvento)

        tvTitulo.text = evento.title
        tvDescricao.text = evento.description ?: "Sem descrição"

        try {
            val dataFormatada = formatoExibicao.format(formatoISO.parse(evento.startTime)!!)
            tvDataHora.text = dataFormatada
        } catch (_: Exception) {
            tvDataHora.text = "Data inválida"
        }

        btnFechar.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }
}
