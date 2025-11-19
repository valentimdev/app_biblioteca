package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.AdminEventosAdapter
import com.example.bibliotecaunifor.databinding.FragmentAdminEventosBinding
import com.example.bibliotecaunifor.services.EventService
import com.example.bibliotecaunifor.services.EventService.toggleAtivoEvento
import com.example.bibliotecaunifor.utils.AuthUtils
import org.json.JSONObject

class AdminEventsFragment : Fragment() {

    private var _binding: FragmentAdminEventosBinding? = null
    private val binding get() = _binding!!

    private val eventos = mutableListOf<AdminEvento>()
    private lateinit var adapter: AdminEventosAdapter

    private var filtroData = false
    private var filtroAlfabetico = false
    private var filtroEncerrados = false
    private var filtroAbertos = false

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
            eventos,
            onSwitchChange = { tipo, evento ->
                if (tipo == "Ativar/Desativar Evento") {
                    val token = AuthUtils.getToken(requireContext())
                    toggleAtivoEvento(token, evento.id!!, evento.isDisabled)
                    carregarEventosDoBackend()
                }
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

    private fun formatarParaISO(dataBr: String): String {
        val partes = dataBr.split("-")
        return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else dataBr
    }

    private fun formatarHorarioISO(data: String, horario: String): String {
        val d = formatarParaISO(data.trim())
        val h = horario.trim()
        return "${d}T${h}:00Z"
    }

    fun obterSeats(edt: android.widget.EditText): Int {
        val valor = edt.text.toString().trim().toIntOrNull() ?: 1
        return if (valor < 1) 1 else valor
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
        aplicarMascaraData(edtDataInicio)
        aplicarMascaraData(edtDataFim)
        val edtDescricao = dlgView.findViewById<android.widget.EditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<android.widget.Switch>(R.id.switchDisabled)

        edtTitulo.setText(evento.title)
        edtLocal.setText(evento.location)
        edtVagas.setText(evento.seats.toString())

        val startParts = evento.startTime.split("T")
        if (startParts.size == 2) {
            val dataIso = startParts[0].split("-")
            if (dataIso.size == 3) edtDataInicio.setText("${dataIso[2]}-${dataIso[1]}-${dataIso[0]}")
            edtHorarioInicio.setText(startParts[1].removeSuffix("Z").substring(0, 5))
        }

        val endParts = evento.endTime.split("T")
        if (endParts.size == 2) {
            val dataIso = endParts[0].split("-")
            if (dataIso.size == 3) edtDataFim.setText("${dataIso[2]}-${dataIso[1]}-${dataIso[0]}")
            edtHorarioFim.setText(endParts[1].removeSuffix("Z").substring(0, 5))
        }

        edtDescricao.setText(evento.description)
        evento.imageUrl?.let { imgPreview?.setImageURI(Uri.parse(it)) }
        switchDisabled.isChecked = evento.isDisabled

        btnEscolherImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Escolher imagem"), REQUEST_IMAGE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { d, _ ->
                val token = AuthUtils.getToken(requireContext())
                val atualizado = AdminEvento(
                    id = evento.id,
                    title = edtTitulo.text.toString().ifBlank { "Evento sem nome" },
                    location = edtLocal.text.toString(),
                    seats = obterSeats(edtVagas),
                    startTime = formatarHorarioISO(edtDataInicio.text.toString(), edtHorarioInicio.text.toString()),
                    endTime = formatarHorarioISO(edtDataFim.text.toString(), edtHorarioFim.text.toString()),
                    isDisabled = switchDisabled.isChecked,
                    description = edtDescricao.text.toString(),
                    imageUrl = selectedImageUri?.toString() ?: evento.imageUrl,
                    lecturers = evento.lecturers,
                    adminId = evento.adminId,
                    createdAt = evento.createdAt,
                    updatedAt = evento.updatedAt
                )

                Thread {
                    try {
                        val json = JSONObject().apply {
                            put("title", atualizado.title)
                            put("location", atualizado.location)
                            put("seats", atualizado.seats)
                            put("startTime", atualizado.startTime)
                            put("endTime", atualizado.endTime)
                            put("isDisabled", atualizado.isDisabled)
                            put("description", atualizado.description)
                            put("image_url", atualizado.imageUrl)
                        }
                        EventService.updateEvent(token, atualizado.id!!, json)
                        carregarEventosDoBackend()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
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
        aplicarMascaraData(edtDataInicio)
        aplicarMascaraData(edtDataFim)
        val edtDescricao = dlgView.findViewById<android.widget.EditText>(R.id.edtDescricao)
        val btnEscolherImagem = dlgView.findViewById<Button>(R.id.btnEscolherImagem)
        imgPreview = dlgView.findViewById(R.id.imgPreview)
        val switchDisabled = dlgView.findViewById<android.widget.Switch>(R.id.switchDisabled)

        btnEscolherImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Escolher imagem"), REQUEST_IMAGE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Novo evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { d, _ ->
                val token = AuthUtils.getToken(requireContext())
                val novo = AdminEvento(
                    id = null,
                    title = edtTitulo.text.toString().ifBlank { "Evento sem nome" },
                    location = edtLocal.text.toString(),
                    seats = obterSeats(edtVagas),
                    startTime = formatarHorarioISO(edtDataInicio.text.toString(), edtHorarioInicio.text.toString()),
                    endTime = formatarHorarioISO(edtDataFim.text.toString(), edtHorarioFim.text.toString()),
                    isDisabled = switchDisabled.isChecked,
                    description = edtDescricao.text.toString(),
                    imageUrl = selectedImageUri?.toString(),
                    lecturers = null,
                    adminId = "",
                    createdAt = "",
                    updatedAt = ""
                )

                Thread {
                    EventService.createEvent(token, novo)
                    carregarEventosDoBackend()
                }.start()
                d.dismiss()
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
                    if (evento.id == null) return@Thread
                    try {
                        EventService.deleteEvent(token, evento.id)
                        carregarEventosDoBackend()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
                dialog.dismiss()
            }
            .setNegativeButton("NÃO", null)
            .show()
    }

    private fun carregarEventosDoBackend() {
        val token = AuthUtils.getToken(requireContext())
        val role = AuthUtils.getRole(requireContext())
        Thread {
            val eventosDto = EventService.getAllEvents(token)
            val lista = eventosDto.mapNotNull {
                val eventId = it.id ?: return@mapNotNull null
                if (!it.isDisabled || role == "ADMIN") {
                    AdminEvento(
                        id = eventId,
                        title = it.title,
                        location = it.location,
                        seats = it.seats ?: 1,
                        startTime = it.startTime,
                        endTime = it.endTime ?: it.startTime,
                        isDisabled = it.isDisabled,
                        description = it.description,
                        imageUrl = it.imageUrl,
                        lecturers = it.lecturers,
                        adminId = "",
                        createdAt = "",
                        updatedAt = ""
                    )
                } else null
            }
            requireActivity().runOnUiThread {
                eventos.clear()
                eventos.addAll(lista)
                adapter.updateData(eventos)
            }
        }.start()
    }

    private fun mostrarDialogoFiltros() {
        val items = arrayOf("Filtrar por data", "Ordem alfabética", "Encerrados", "Abertos")
        val checkedItems = booleanArrayOf(filtroData, filtroAlfabetico, filtroEncerrados, filtroAbertos)
        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar eventos")
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Aplicar") { _, _ ->
                filtroData = checkedItems[0]
                filtroAlfabetico = checkedItems[1]
                filtroEncerrados = checkedItems[2]
                filtroAbertos = checkedItems[3]
                aplicarFiltros()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aplicarFiltros() {
        var filtrados = eventos.toList()
        if (filtroData) filtrados = filtrados.sortedBy { it.startTime }
        if (filtroAlfabetico) filtrados = filtrados.sortedBy { it.title }
        adapter.updateData(filtrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
