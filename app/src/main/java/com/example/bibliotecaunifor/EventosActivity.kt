package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.bibliotecaunifor.adapters.EventosAdapter
import com.example.bibliotecaunifor.databinding.ActivityEventosBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class EventosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventosBinding
    private lateinit var todosEventos: List<Evento>
    private var diaSelecionado: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar sem back arrow q
        setSupportActionBar(binding.toolbarEventos)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbarEventos.navigationIcon = null

        // Configura RecyclerView
        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(this)

        // Eventos de exemplo
        todosEventos = listOf(
            Evento(
                "Palestra de Literatura",
                Calendar.getInstance().apply { set(2025, 9, 12, 14, 0) },
                "Uma palestra sobre obras clássicas e modernas da literatura."
            ),
            Evento(
                "Clube de Leitura",
                Calendar.getInstance().apply { set(2025, 9, 18, 18, 0) },
                "Encontro semanal para discutir livros escolhidos pelo grupo."
            ),
            Evento(
                "Oficina de Escrita",
                Calendar.getInstance().apply { set(2025, 9, 18, 10, 0) },
                "Aprenda técnicas de escrita criativa e desenvolvimento de personagens."
            )
        )

        atualizarEventosNoCalendario()

        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val dataSelecionada = eventDay.calendar
                if (diaSelecionado != null && ehMesmoDia(diaSelecionado!!, dataSelecionada)) {
                    diaSelecionado = null
                    atualizarEventosNoCalendario()
                    mostrarEventos(todosEventos)
                } else {
                    // novo dia selecionado
                    diaSelecionado = dataSelecionada
                    atualizarEventosNoCalendario()
                    filtrarEventosPorDia(dataSelecionada)
                }
            }
        })

        mostrarEventos(todosEventos)
    }

    private fun atualizarEventosNoCalendario() {
        val eventos = todosEventos.map {
            EventDay(it.data, R.drawable.ic_event_marker)
        }.toMutableList()

        diaSelecionado?.let {
            eventos.add(EventDay(it, R.drawable.ic_day_selected))
        }

        binding.calendarView.setEvents(eventos)
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
        binding.recyclerViewEventos.adapter = EventosAdapter(eventos) { evento ->
            mostrarDetalhesEvento(evento)
        }
    }

    private fun mostrarDetalhesEvento(evento: Evento) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_info_evento, null)

        view.findViewById<TextView>(R.id.tvTituloEvento).text = evento.titulo
        view.findViewById<TextView>(R.id.tvDescricaoEvento).text = evento.descricao
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        view.findViewById<TextView>(R.id.tvDataHoraEvento).text = formato.format(evento.data.time)

        val btnInscrever = view.findViewById<Button>(R.id.btnInscreverEvento)
        val chipInscrito = view.findViewById<Chip>(R.id.chipInscrito)

        if (evento.inscrito) {
            btnInscrever.visibility = View.GONE
            chipInscrito.visibility = View.VISIBLE
        } else {
            btnInscrever.visibility = View.VISIBLE
            chipInscrito.visibility = View.GONE
            btnInscrever.setOnClickListener {
                evento.inscrito = true
                btnInscrever.visibility = View.GONE
                chipInscrito.visibility = View.VISIBLE
                dialog.dismiss()
            }
        }

        val buttonFechar = view.findViewById<Button>(R.id.buttonFecharEvento)
        buttonFechar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
