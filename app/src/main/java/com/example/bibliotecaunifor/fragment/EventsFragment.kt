package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.EventosAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.example.bibliotecaunifor.notifications.NotificationHelper

class EventsFragment : Fragment(R.layout.fragment_events) {

    private lateinit var recyclerViewEventos: androidx.recyclerview.widget.RecyclerView
    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView

    private var todosEventos: List<Evento> = emptyList()
    private var eventosInscritos: Set<String> = emptySet() // IDs dos eventos que o usuário está inscrito
    private var diaSelecionado: Calendar? = null

    private val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val formatoExibicao = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewEventos = view.findViewById(R.id.recyclerViewEventos)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())

        carregarEventosEInscricoes()

        // Listener para clique no dia do calendário
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val diaClicado = eventDay.calendar
                val hoje = Calendar.getInstance()

                val ehMesmoDiaSelecionado = diaSelecionado?.let { ehMesmoDia(it, diaClicado) } == true

                if (ehMesmoDiaSelecionado) {
                    // Volta a mostrar todos os eventos futuros (a partir de hoje)
                    diaSelecionado = null

                    val eventosFuturos = todosEventos.filter { evento ->
                        try {
                            val dataEvento = Calendar.getInstance().apply {
                                time = formatoISO.parse(evento.startTime)!!
                            }
                            !dataEvento.before(hoje) // inclui hoje e futuros
                        } catch (e: Exception) {
                            false
                        }
                    }

                    mostrarEventos(eventosFuturos)
                    Toast.makeText(requireContext(), "Mostrando eventos futuros", Toast.LENGTH_SHORT).show()
                } else {
                    diaSelecionado = diaClicado.clone() as Calendar
                    filtrarEventosPorDia(diaClicado)
                }

                atualizarEventosNoCalendario()
                recyclerViewEventos.scrollToPosition(0)
            }
        })
    }

    private fun carregarEventosEInscricoes() {
        // Primeiro carrega TODOS os eventos
        RetrofitClient.eventApi.getAllEvents().enqueue(object : Callback<List<Evento>> {
            override fun onResponse(call: Call<List<Evento>>, response: Response<List<Evento>>) {
                if (response.isSuccessful) {
                    // Só eventos ativos aparecem para o aluno
                    todosEventos = (response.body() ?: emptyList())
                        .filter { !it.isDisabled }

                    atualizarEventosNoCalendario()
                    mostrarEventos(todosEventos)
                }

            }

            override fun onFailure(call: Call<List<Evento>>, t: Throwable) {}
        })

        // Depois carrega os eventos que o usuário está inscrito (via /me ou /events/:id/status)
        RetrofitClient.userApi.getMe().enqueue(object : Callback<com.example.bibliotecaunifor.models.UserResponse> {
            override fun onResponse(call: Call<com.example.bibliotecaunifor.models.UserResponse>, response: Response<com.example.bibliotecaunifor.models.UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    eventosInscritos = response.body()!!.events.map { it.id }.toSet()
                    recyclerViewEventos.adapter?.notifyDataSetChanged() // Atualiza botões
                }
            }

            override fun onFailure(call: Call<com.example.bibliotecaunifor.models.UserResponse>, t: Throwable) {}
        })
    }

    private fun atualizarEventosNoCalendario() {
        val eventosMarcados = todosEventos.mapNotNull { evento ->
            try {
                val cal = Calendar.getInstance().apply {
                    time = formatoISO.parse(evento.startTime)!!
                }
                EventDay(cal, R.drawable.ic_event_marker)
            } catch (e: Exception) {
                null
            }
        }.toMutableList()

        diaSelecionado?.let {
            eventosMarcados.add(EventDay(it, R.drawable.ic_day_selected))
        }

        calendarView.setEvents(eventosMarcados)
    }

    private fun ehMesmoDia(c1: Calendar, c2: Calendar): Boolean =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)

    private fun filtrarEventosPorDia(dataSelecionada: Calendar) {
        val eventosDoDia = todosEventos.filter { evento ->
            try {
                val calEvento = Calendar.getInstance().apply {
                    time = formatoISO.parse(evento.startTime)!!
                }
                ehMesmoDia(calEvento, dataSelecionada)
            } catch (e: Exception) {
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
        val btnAction = view.findViewById<Button>(R.id.btnInscreverEvento)
        val chipInscrito = view.findViewById<Chip>(R.id.chipInscrito)

        tvTitulo.text = evento.title
        tvDescricao.text = evento.description ?: "Sem descrição"
        tvDataHora.text = try {
            formatoExibicao.format(formatoISO.parse(evento.startTime)!!)
        } catch (e: Exception) {
            "Data inválida"
        }

        btnFechar.setOnClickListener { dialog.dismiss() }

        val estaInscrito = eventosInscritos.contains(evento.id)

        if (estaInscrito) {
            chipInscrito.visibility = View.VISIBLE
            btnAction.text = "CANCELAR INSCRIÇÃO"
            btnAction.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
            btnAction.setOnClickListener {
                confirmarCancelamento(evento, dialog)
            }
         } else {
        chipInscrito.visibility = View.GONE

        // 1) Verifica se a inscrição está aberta (switch "Inscrição" do admin)
        val agora = Date()
        val inscricaoAberta = try {
            val inicio = evento.registrationStartTime?.let { formatoISO.parse(it) }
            val fim = evento.registrationEndTime?.let { formatoISO.parse(it) }

            inicio != null && fim != null &&
                    agora.after(inicio) && agora.before(fim)
        } catch (e: Exception) {
            false
        }

        when {
            !inscricaoAberta -> {
                btnAction.text = "INSCRIÇÕES FECHADAS"
                btnAction.isEnabled = false
            }
            evento.seats <= 0 -> {
                btnAction.text = "LOTADO"
                btnAction.isEnabled = false
            }
            else -> {
                btnAction.text = "INSCREVER-SE"
                btnAction.isEnabled = true
                btnAction.setBackgroundColor(
                    resources.getColor(
                        com.google.android.material.R.color.material_dynamic_primary60,
                        null
                    )
                )
                btnAction.setOnClickListener {
                    inscreverNoEvento(evento, btnAction, chipInscrito)
                }
            }
        }
    }


    dialog.setContentView(view)
        dialog.show()
    }

    private fun inscreverNoEvento(evento: Evento, btn: Button, chip: Chip) {
        btn.isEnabled = false
        btn.text = "Inscrevendo..."

        RetrofitClient.eventApi.register(evento.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Inscrito com sucesso!", Toast.LENGTH_LONG).show()
                    eventosInscritos = eventosInscritos + evento.id
                    chip.visibility = View.VISIBLE
                    btn.text = "CANCELAR INSCRIÇÃO"
                    btn.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
                    btn.setOnClickListener { confirmarCancelamento(evento, null) }
                    (requireActivity() as? MainActivity)?.refreshHomeFragment()

                    // 🔔 Notificação imediata
                    NotificationHelper.showNotification(
                        requireContext(),
                        "Inscrição confirmada",
                        "Você se inscreveu no evento \"${evento.title}\"."
                    )

                    // ⏰ Agenda lembrete 24h antes do evento
                    NotificationHelper.scheduleEventReminder24hBefore(requireContext(), evento)

                } else {
                    btn.isEnabled = true
                    btn.text = "INSCREVER-SE"
                    Toast.makeText(requireContext(), "Erro ao se inscrever", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btn.isEnabled = true
                btn.text = "INSCREVER-SE"
                Toast.makeText(requireContext(), "Sem conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun confirmarCancelamento(evento: Evento, dialog: BottomSheetDialog?) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancelar inscrição")
            .setMessage("Tem certeza que deseja cancelar sua inscrição em:\n\n\"${evento.title}\"?")
            .setPositiveButton("Sim, cancelar") { _, _ ->
                cancelarInscricao(evento, dialog)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun cancelarInscricao(evento: Evento, dialog: BottomSheetDialog?) {
        RetrofitClient.eventApi.unregister(evento.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Inscrição cancelada!", Toast.LENGTH_LONG).show()
                    eventosInscritos = eventosInscritos - evento.id
                    dialog?.dismiss()
                    carregarEventosEInscricoes()
                    (requireActivity() as? MainActivity)?.refreshHomeFragment()

                    // ❌ Cancela lembrete 24h se existir
                    NotificationHelper.cancelEventReminder(requireContext(), evento.id)

                    // 🔔 Notificação imediata de cancelamento
                    NotificationHelper.showNotification(
                        requireContext(),
                        "Inscrição cancelada",
                        "Você cancelou sua inscrição no evento \"${evento.title}\"."
                    )
                } else {
                    Toast.makeText(requireContext(), "Erro ao cancelar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Sem conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }



}