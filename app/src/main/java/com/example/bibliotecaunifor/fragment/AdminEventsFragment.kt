package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.AdminEventosAdapter
import com.example.bibliotecaunifor.api.EventService
import com.example.bibliotecaunifor.databinding.FragmentAdminEventosBinding
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminEventosAdapter(
            requireContext(),
            eventos,
            onSwitchChange = { tipo, isChecked, evento ->
                Thread {
                    val token = AuthUtils.getToken(requireContext())

                    when (tipo) {
                        "Ativar/Desativar Evento" -> {
                            // switch ligado (true) = evento visível para o aluno
                            // no backend: isDisabled = !isChecked
                            EventService.toggleAtivoEvento(
                                token,
                                evento.id,
                                !isChecked
                            )
                        }

                        "Permitir Inscrição" -> {
                            // Abrir/fechar inscrição usando registrationStartTime / registrationEndTime
                            val iso = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }

                            val dto = JSONObject().apply {
                                if (isChecked) {
                                    // Liga inscrição: de AGORA até o início do evento
                                    put("registrationStartTime", iso.format(Date()))
                                    put("registrationEndTime", evento.eventStartTime)
                                } else {
                                    // Desliga inscrição
                                    put("registrationStartTime", JSONObject.NULL)
                                    put("registrationEndTime", JSONObject.NULL)
                                }
                            }

                            EventService.updateEvent(token, evento.id, dto)
                        }
                    }

                    requireActivity().runOnUiThread { carregarEventosDoBackend() }
                }.start()
            },

            onItemClick = { mostrarDialogoEvento(it) }
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

    // MÁSCARA DE DATA
    private fun aplicarMascaraData(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val digits = s.toString().replace("-", "")
                if (digits.length > 8) {
                    editText.setText(digits.take(8))
                    editText.setSelection(8)
                    return
                }
                val formatted = buildString {
                    for (i in digits.indices) {
                        if (i == 2 || i == 4) append("-")
                        append(digits[i])
                    }
                }
                isUpdating = true
                editText.setText(formatted)
                editText.setSelection(formatted.length)
                isUpdating = false
            }
        })
    }

    // MÁSCARA DE HORÁRIO
    private fun aplicarMascaraHorario(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val digits = s.toString().replace(":", "")
                if (digits.length > 4) {
                    editText.setText(digits.take(4))
                    editText.setSelection(4)
                    return
                }
                val formatted = when (digits.length) {
                    0 -> ""
                    1 -> "0${digits}:"
                    2 -> "${digits}:"
                    3 -> "${digits.substring(0, 2)}:${digits[2]}"
                    4 -> "${digits.substring(0, 2)}:${digits.substring(2)}"
                    else -> ""
                }
                isUpdating = true
                editText.setText(formatted)
                editText.setSelection(formatted.length)
                isUpdating = false
            }
        })
    }

    private fun formatarISO(dataStr: String, horaStr: String): String? {
        val dataLimpa = dataStr.replace("-", "").trim()
        val horaLimpa = horaStr.replace(":", "").trim().padStart(4, '0').take(4)

        if (dataLimpa.length != 8 || horaLimpa.length != 4) return null

        val inputStr = "${dataLimpa.substring(0, 2)}-${dataLimpa.substring(2, 4)}-${dataLimpa.substring(4, 8)} " +
                "${horaLimpa.substring(0, 2)}:${horaLimpa.substring(2, 4)}"

        return try {
            val inFmt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            outFmt.format(inFmt.parse(inputStr)!!)
        } catch (e: Exception) {
            Log.e("FORMAT_ISO", "Erro ao formatar: $inputStr", e)
            null
        }
    }

    // ==== helper: agora em ISO UTC para registrationStartTime ====
    private fun agoraIsoUtc(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }

    private fun obterSeats(edt: TextInputEditText): Int =
        edt.text.toString().trim().toIntOrNull()?.coerceAtLeast(1) ?: 1

    // ====================== EDITAR EVENTO ======================
    private fun mostrarDialogEditar(evento: AdminEvento) {
        selectedImageUri = null
        val dlgView = layoutInflater.inflate(R.layout.dialog_admin_evento, null)

        val edtTitulo = dlgView.findViewById<TextInputEditText>(R.id.edtTitulo)
        val edtLocal = dlgView.findViewById<TextInputEditText>(R.id.edtLocal)
        val edtVagas = dlgView.findViewById<TextInputEditText>(R.id.edtVagas)
        val edtDataInicio = dlgView.findViewById<TextInputEditText>(R.id.edtDataInicio)
        val edtHorarioInicio = dlgView.findViewById<TextInputEditText>(R.id.edtHorarioInicio)
        val edtDataFim = dlgView.findViewById<TextInputEditText>(R.id.edtDataFim)
        val edtHorarioFim = dlgView.findViewById<TextInputEditText>(R.id.edtHorarioFim)
        val edtDescricao = dlgView.findViewById<TextInputEditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<MaterialSwitch>(R.id.switchDisabled)

        aplicarMascaraData(edtDataInicio); aplicarMascaraData(edtDataFim)
        aplicarMascaraHorario(edtHorarioInicio); aplicarMascaraHorario(edtHorarioFim)

        edtTitulo.setText(evento.title)
        edtLocal.setText(evento.location ?: "")
        edtVagas.setText(evento.seats.toString())
        edtDescricao.setText(evento.description ?: "")
        switchDisabled.isChecked = evento.isDisabled

        evento.eventStartTime.split("T").let {
            val d = it[0].split("-")
            edtDataInicio.setText("${d[2]}-${d[1]}-${d[0]}")
            edtHorarioInicio.setText(it[1].removeSuffix("Z").substring(0, 5))
        }

        evento.eventEndTime?.takeIf { it.isNotBlank() }?.split("T")?.let {
            val d = it[0].split("-")
            edtDataFim.setText("${d[2]}-${d[1]}-${d[0]}")
            edtHorarioFim.setText(it[1].removeSuffix("Z").substring(0, 5))
        }

        evento.imageUrl?.let { Glide.with(this).load(it).into(imgPreview!!) }

        btnEscolherImagem.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
                REQUEST_IMAGE
            )
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { _, _ ->
                val startISO = formatarISO(
                    edtDataInicio.text.toString(),
                    edtHorarioInicio.text.toString()
                )
                if (startISO == null) {
                    Toast.makeText(
                        requireContext(),
                        "Data/hora início inválida",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val endISO = if (edtDataFim.text.isNullOrBlank()) null
                else formatarISO(edtDataFim.text.toString(), edtHorarioFim.text.toString())

                val json = JSONObject().apply {
                    put("title", edtTitulo.text.toString())
                    put(
                        "description",
                        edtDescricao.text.toString().ifBlank { JSONObject.NULL }
                    )
                    put("location", edtLocal.text.toString().ifBlank { JSONObject.NULL })
                    put("eventStartTime", startISO)
                    put("eventEndTime", endISO ?: JSONObject.NULL)
                    put("seats", obterSeats(edtVagas))
                    put("isDisabled", switchDisabled.isChecked)
                }

                Thread {
                    EventService.updateEvent(
                        AuthUtils.getToken(requireContext()),
                        evento.id,
                        json
                    )

                    selectedImageUri?.let { uri ->
                        EventService.uploadEventImage(
                            requireContext(),
                            AuthUtils.getToken(requireContext()),
                            evento.id,
                            uri
                        )
                    }

                    requireActivity().runOnUiThread {
                        carregarEventosDoBackend()
                        Toast.makeText(
                            requireContext(),
                            "Evento atualizado!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.start()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ====================== ADICIONAR EVENTO ======================
    private fun mostrarDialogAdicionar() {
        selectedImageUri = null
        val dlgView = layoutInflater.inflate(R.layout.dialog_admin_evento, null)

        val edtTitulo = dlgView.findViewById<TextInputEditText>(R.id.edtTitulo)
        val edtLocal = dlgView.findViewById<TextInputEditText>(R.id.edtLocal)
        val edtVagas = dlgView.findViewById<TextInputEditText>(R.id.edtVagas)
        val edtDataInicio = dlgView.findViewById<TextInputEditText>(R.id.edtDataInicio)
        val edtHorarioInicio = dlgView.findViewById<TextInputEditText>(R.id.edtHorarioInicio)
        val edtDataFim = dlgView.findViewById<TextInputEditText>(R.id.edtDataFim)
        val edtHorarioFim = dlgView.findViewById<TextInputEditText>(R.id.edtHorarioFim)
        val edtDescricao = dlgView.findViewById<TextInputEditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<MaterialSwitch>(R.id.switchDisabled)

        aplicarMascaraData(edtDataInicio); aplicarMascaraData(edtDataFim)
        aplicarMascaraHorario(edtHorarioInicio); aplicarMascaraHorario(edtHorarioFim)

        btnEscolherImagem.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
                REQUEST_IMAGE
            )
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Novo evento")
            .setView(dlgView)
            .setPositiveButton("Criar") { _, _ ->
                val startISO = formatarISO(
                    edtDataInicio.text.toString(),
                    edtHorarioInicio.text.toString()
                )
                if (startISO == null) {
                    Toast.makeText(
                        requireContext(),
                        "Data/hora de início inválida",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val endISO = if (edtDataFim.text.isNullOrBlank()) null
                else formatarISO(edtDataFim.text.toString(), edtHorarioFim.text.toString())

                val novoEvento = AdminEvento(
                    id = "",
                    title = edtTitulo.text.toString().ifBlank { "Evento sem título" },
                    description = edtDescricao.text.toString().ifBlank { null },
                    registrationStartTime = null,
                    registrationEndTime = null,
                    eventStartTime = startISO,
                    eventEndTime = endISO,
                    startTime = null,
                    endTime = null,
                    location = edtLocal.text.toString().ifBlank { null },
                    imageUrl = null,
                    lecturers = null,
                    seats = obterSeats(edtVagas),
                    isDisabled = switchDisabled.isChecked,
                    isFull = false,
                    adminId = null,
                    createdAt = null,
                    updatedAt = null
                )

                Thread {
                    EventService.createEvent(
                        requireContext(),
                        AuthUtils.getToken(requireContext()),
                        novoEvento,
                        selectedImageUri
                    )
                    requireActivity().runOnUiThread {
                        carregarEventosDoBackend()
                        Toast.makeText(
                            requireContext(),
                            "Evento criado com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.start()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            imgPreview?.setImageURI(selectedImageUri)
        }
    }

    private fun excluirEvento(evento: AdminEvento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir evento")
            .setMessage("Tem certeza que deseja excluir \"${evento.title}\"?")
            .setPositiveButton("Sim") { _, _ ->
                Thread {
                    EventService.deleteEvent(AuthUtils.getToken(requireContext()), evento.id)
                    requireActivity().runOnUiThread { carregarEventosDoBackend() }
                }.start()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun carregarEventosDoBackend() {
        Thread {
            val lista = EventService.getAllEvents(AuthUtils.getToken(requireContext())).map {
                AdminEvento(
                    id = it.id ?: "",
                    title = it.title,
                    description = it.description,
                    eventStartTime = it.eventStartTime,
                    eventEndTime = it.eventEndTime,
                    registrationStartTime = it.registrationStartTime,
                    registrationEndTime = it.registrationEndTime,
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
                aplicarFiltros()
            }
        }.start()
    }

    private fun mostrarDialogoFiltros() {
        val items = arrayOf("Ordenar por data", "Ordem alfabética")
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
        if (filtroAlfabetico) filtrados = filtrados.sortedBy { it.title.lowercase() }
        adapter.updateData(filtrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
