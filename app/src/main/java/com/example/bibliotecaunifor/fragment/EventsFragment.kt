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
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class EventsFragment : Fragment(R.layout.fragment_events) {

    private lateinit var recyclerViewEventos: androidx.recyclerview.widget.RecyclerView
    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private var todosEventos: List<Evento> = listOf()
    private var diaSelecionado: Calendar? = null

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewEventos = view.findViewById(R.id.recyclerViewEventos)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())

        Thread {
            val eventos = EventApi.fetchEventos()
            requireActivity().runOnUiThread {
                todosEventos = eventos
                atualizarEventosNoCalendario()
                mostrarEventos(todosEventos)
            }
        }.start()

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

    private fun atualizarEventosNoCalendario() {
        val eventos = todosEventos.map { EventDay(it.data, R.drawable.ic_event_marker) }.toMutableList()
        diaSelecionado?.let { eventos.add(EventDay(it, R.drawable.ic_day_selected)) }
        calendarView.setEvents(eventos)
    }

    private fun ehMesmoDia(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }

    private fun filtrarEventosPorDia(dataSelecionada: Calendar) {
        val eventosDoDia = todosEventos.filter { ehMesmoDia(it.data, dataSelecionada) }
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
        val btnInscrever = view.findViewById<Button>(R.id.btnInscreverEvento)
        val chipInscrito = view.findViewById<Chip>(R.id.chipInscrito)
        val btnFechar = view.findViewById<Button>(R.id.buttonFecharEvento)

        tvTitulo.text = evento.titulo
        tvDescricao.text = evento.descricao
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvDataHora.text = formato.format(evento.data.time)

        atualizarEstadoInscricao(evento.inscrito, btnInscrever, chipInscrito)

        // alterna inscrição/cancelamento
        btnInscrever.setOnClickListener {
            evento.inscrito = !evento.inscrito
            atualizarEstadoInscricao(evento.inscrito, btnInscrever, chipInscrito)

            if (evento.inscrito) {
                btnInscrever.text = "Cancelar inscrição"
            } else {
                btnInscrever.text = "Inscrever-se"
            }
        }

        btnFechar.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun atualizarEstadoInscricao(
        inscrito: Boolean,
        btnInscrever: Button,
        chip: Chip
    ) {
        if (inscrito) {
            btnInscrever.text = "Cancelar inscrição"
            chip.visibility = View.VISIBLE
        } else {
            btnInscrever.text = "Inscrever-se"
            chip.visibility = View.GONE
        }
    }

}
