package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.AdminEventosAdapter
import com.example.bibliotecaunifor.databinding.FragmentAdminEventosBinding
import com.example.bibliotecaunifor.api.EventService
import com.example.bibliotecaunifor.utils.AuthUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdminEventsFragment : Fragment() {

    private var _binding: FragmentAdminEventosBinding? = null
    private val binding get() = _binding!!

    private val eventos = mutableListOf<AdminEvento>()
    private lateinit var adapter: AdminEventosAdapter

    private var filtroData = false
    private var filtroAlfabetico = false

    private var selectedImageUri: Uri? = null
    private var imgPreview: ImageView? = null

    companion object {
        const val REQUEST_IMAGE = 1001
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.configureToolbarFor(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminEventosAdapter(
            requireContext(),
            eventos,
            onSwitchChange = { _, evento ->
                val token = AuthUtils.getToken(requireContext())
                EventService.toggleAtivoEvento(token, evento.id, evento.isDisabled)
                carregarEventosDoBackend()
            },
            onItemClick = { evento ->
                mostrarDialogoEvento(evento)
            }
        )

        binding.recyclerAdminEventos.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAdminEventos.adapter = adapter

        carregarEventosDoBackend()

        binding.btnFiltrarEvento.setOnClickListener { mostrarDialogoFiltros() }
        binding.btnAdicionarEvento.setOnClickListener { mostrarDialogAdicionar() }

        binding.etBuscarEvento.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtrados = eventos.filter { it.title.lowercase().contains(query) }
            adapter.updateData(filtrados)
        }
    }

    private fun mostrarDialogoEvento(evento: AdminEvento) {
        AlertDialog.Builder(requireContext())
            .setTitle(evento.title)
            .setItems(arrayOf("Editar", "Excluir")) { _, which ->
                when (which) {
                    0 -> mostrarDialogEditar(evento)
                    1 -> excluirEvento(evento)
                }
            }
            .show()
    }

    private fun aplicarMascaraData(editText: android.widget.EditText) {
        var isUpdating = false
        editText.addTextChangedListener {
            if (isUpdating) return@addTextChangedListener
            val str = it.toString().replace("-", "")
            if (str.length > 8) return@addTextChangedListener
            val sb = StringBuilder()
            if (str.length > 2) sb.append(str.substring(0, 2)).append("-") else sb.append(str)
            if (str.length > 4) sb.append(str.substring(2, 4)).append("-") else if (str.length > 2) sb.append(str.substring(2))
            if (str.length > 4) sb.append(str.substring(4))
            isUpdating = true
            editText.setText(sb.toString())
            editText.setSelection(editText.text.length)
            isUpdating = false
        }
    }

    private fun aplicarMascaraHorario(editText: android.widget.EditText) {
        var isUpdating = false
        editText.addTextChangedListener {
            if (isUpdating) return@addTextChangedListener
            val str = it.toString().replace(":", "").take(4)
            val formatted = buildString {
                for (i in str.indices) {
                    if (i == 2) append(":")
                    append(str[i])
                }
            }
            isUpdating = true
            editText.setText(formatted)
            editText.setSelection(formatted.length)
            isUpdating = false
        }
    }

    private fun formatarISO(d: String, h: String): String {
        val inFmt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        inFmt.timeZone = TimeZone.getTimeZone("UTC")
        val outFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        outFmt.timeZone = TimeZone.getTimeZone("UTC")
        val date = inFmt.parse("$d $h")
        return outFmt.format(date!!)
    }

    fun obterSeats(edt: android.widget.EditText): Int {
        return edt.text.toString().trim().toIntOrNull()?.coerceAtLeast(1) ?: 1
    }

    private fun mostrarDialogEditar(evento: AdminEvento) {
        selectedImageUri = null
        val dlgView = layoutInflater.inflate(R.layout.dialog_admin_evento, null)
        val edtTitulo = dlgView.findViewById<android.widget.EditText>(R.id.edtTitulo)
        val edtLocal = dlgView.findViewById<android.widget.EditText>(R.id.edtLocal)
        val edtVagas = dlgView.findViewById<android.widget.EditText>(R.id.edtVagas)
        val edtDataInicio = dlgView.findViewById<android.widget.EditText>(R.id.edtDataInicio)
        val edtHorarioInicio = dlgView.findViewById<android.widget.EditText>(R.id.edtHorarioInicio)
        val edtDataFim = dlgView.findViewById<android.widget.EditText>(R.id.edtDataFim)
        val edtHorarioFim = dlgView.findViewById<android.widget.EditText>(R.id.edtHorarioFim)
        val edtDescricao = dlgView.findViewById<android.widget.EditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<android.widget.Switch>(R.id.switchDisabled)

        aplicarMascaraData(edtDataInicio)
        aplicarMascaraData(edtDataFim)
        aplicarMascaraHorario(edtHorarioInicio)
        aplicarMascaraHorario(edtHorarioFim)

        edtTitulo.setText(evento.title)
        edtLocal.setText(evento.location)
        edtVagas.setText(evento.seats.toString())

        evento.eventStartTime.split("T").let {
            val d = it[0].split("-")
            edtDataInicio.setText("${d[2]}-${d[1]}-${d[0]}")
            edtHorarioInicio.setText(it[1].removeSuffix("Z").substring(0, 5))
        }

        evento.eventEndTime?.split("T")?.let {
            val d = it[0].split("-")
            edtDataFim.setText("${d[2]}-${d[1]}-${d[0]}")
            edtHorarioFim.setText(it[1].removeSuffix("Z").substring(0, 5))
        }

        edtDescricao.setText(evento.description)
        evento.imageUrl?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .into(imgPreview!!)
        }
        switchDisabled.isChecked = evento.isDisabled

        btnEscolherImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { dialog, _ ->

                val eventStartISO = formatarISO(edtDataInicio.text.toString(), edtHorarioInicio.text.toString())
                val eventEndISO = formatarISO(edtDataFim.text.toString(), edtHorarioFim.text.toString())

                val dto = JSONObject().apply {
                    put("title", edtTitulo.text.toString())
                    put("description", edtDescricao.text.toString())
                    put("location", edtLocal.text.toString())
                    put("eventStartTime", eventStartISO)
                    put("eventEndTime", eventEndISO)

                    put(
                        "registrationStartTime",
                        if (evento.registrationStartTime.isNullOrBlank() || evento.registrationStartTime == "null")
                            JSONObject.NULL
                        else evento.registrationStartTime
                    )

                    put(
                        "registrationEndTime",
                        if (evento.registrationEndTime.isNullOrBlank() || evento.registrationEndTime == "null")
                            JSONObject.NULL
                        else evento.registrationEndTime
                    )

                    put("seats", obterSeats(edtVagas))
                    put("isDisabled", switchDisabled.isChecked)

                    put("lecturers",
                        evento.lecturers ?: JSONObject.NULL
                    )
                    put("isFull", evento.isFull)
                }
                Log.i("UPDATE_EVENT_JSON", dto.toString())
                val token = AuthUtils.getToken(requireContext())

                Thread {
                    EventService.updateEvent(token, evento.id, dto)
                    carregarEventosDoBackend()
                }.start()

                dialog.dismiss()
            }            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogAdicionar() {
        selectedImageUri = null
        val dlgView = layoutInflater.inflate(R.layout.dialog_admin_evento, null)
        val edtTitulo = dlgView.findViewById<android.widget.EditText>(R.id.edtTitulo)
        val edtLocal = dlgView.findViewById<android.widget.EditText>(R.id.edtLocal)
        val edtVagas = dlgView.findViewById<android.widget.EditText>(R.id.edtVagas)
        val edtDataInicio = dlgView.findViewById<android.widget.EditText>(R.id.edtDataInicio)
        val edtHorarioInicio = dlgView.findViewById<android.widget.EditText>(R.id.edtHorarioInicio)
        val edtDataFim = dlgView.findViewById<android.widget.EditText>(R.id.edtDataFim)
        val edtHorarioFim = dlgView.findViewById<android.widget.EditText>(R.id.edtHorarioFim)
        val edtDescricao = dlgView.findViewById<android.widget.EditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<android.widget.Switch>(R.id.switchDisabled)

        aplicarMascaraData(edtDataInicio)
        aplicarMascaraData(edtDataFim)
        aplicarMascaraHorario(edtHorarioInicio)
        aplicarMascaraHorario(edtHorarioFim)

        btnEscolherImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Novo evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { dialog, _ ->

                val eventStartISO = formatarISO(edtDataInicio.text.toString(), edtHorarioInicio.text.toString())
                val eventEndISO = formatarISO(edtDataFim.text.toString(), edtHorarioFim.text.toString())

                val novo = AdminEvento(
                    id = "",
                    title = edtTitulo.text.toString(),
                    description = edtDescricao.text.toString(),
                    registrationStartTime = null,
                    registrationEndTime = null,
                    eventStartTime = eventStartISO,
                    eventEndTime = eventEndISO,
                    startTime = null,
                    endTime = null,
                    location = edtLocal.text.toString(),
                    imageUrl = selectedImageUri?.toString(),
                    lecturers = null,
                    seats = obterSeats(edtVagas),
                    isDisabled = switchDisabled.isChecked,
                    isFull = false,
                    adminId = null,
                    createdAt = null,
                    updatedAt = null
                )

                val token = AuthUtils.getToken(requireContext())

                Thread {
                    EventService.createEvent(requireContext(), token, novo, selectedImageUri)
                    carregarEventosDoBackend()
                }.start()

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imgPreview?.setImageURI(selectedImageUri)
        }
    }

    private fun excluirEvento(evento: AdminEvento) {
        val token = AuthUtils.getToken(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir evento")
            .setMessage("Deseja realmente excluir o evento ${evento.title}?")
            .setPositiveButton("SIM") { dialog, _ ->
                Thread {
                    EventService.deleteEvent(token, evento.id)
                    carregarEventosDoBackend()
                }.start()
                dialog.dismiss()
            }
            .setNegativeButton("NÃO", null)
            .show()
    }

    private fun carregarEventosDoBackend() {
        val token = AuthUtils.getToken(requireContext())

        Thread {
            val lista = EventService.getAllEvents(token).map {
                AdminEvento(
                    id = it.id ?: "",
                    title = it.title,
                    description = it.description,
                    registrationStartTime = it.registrationStartTime,
                    registrationEndTime = it.registrationEndTime,
                    eventStartTime = it.eventStartTime,
                    eventEndTime = it.eventEndTime,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    location = it.location,
                    imageUrl = it.imageUrl,
                    lecturers = it.lecturers,
                    seats = it.seats ?: 0,
                    isDisabled = it.isDisabled,
                    isFull = it.isFull,
                    adminId = it.adminId ?: "",
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }

            requireActivity().runOnUiThread {
                eventos.clear()
                eventos.addAll(lista)
                adapter.updateData(eventos)
            }
        }.start()
    }

    private fun mostrarDialogoFiltros() {
        val items = arrayOf("Filtrar por data", "Ordem alfabética")
        val checked = booleanArrayOf(filtroData, filtroAlfabetico)

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar eventos")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Aplicar") { _, _ ->
                filtroData = checked[0]
                filtroAlfabetico = checked[1]
                aplicarFiltros()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aplicarFiltros() {
        var filtrados = eventos.toList()
        if (filtroData) filtrados = filtrados.sortedBy { it.eventStartTime }
        if (filtroAlfabetico) filtrados = filtrados.sortedBy { it.title }
        adapter.updateData(filtrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
