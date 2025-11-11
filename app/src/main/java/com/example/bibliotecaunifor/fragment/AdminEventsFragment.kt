package com.example.bibliotecaunifor.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bibliotecaunifor.AdminEvento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.AdminEventosAdapter
import com.example.bibliotecaunifor.databinding.FragmentAdminEventosBinding

class AdminEventsFragment : Fragment() {

    private var _binding: FragmentAdminEventosBinding? = null
    private val binding get() = _binding!!

    // agora é mutável
    private val eventos = mutableListOf(
        AdminEvento("Palestra de Literatura", "Auditório A", 50, "12/09/2025", "14:00"),
        AdminEvento("Feira de Tecnologia", "Bloco H", 80, "20/09/2025", "10:00"),
        AdminEvento("Semana de Sustentabilidade", "Auditório B", 30, "03/10/2025", "09:00"),
        AdminEvento("Workshop de Empreendedorismo", "Sala 205", 20, "15/10/2025", "13:30")
    )

    private lateinit var adapter: AdminEventosAdapter

    private var filtroData = false
    private var filtroAlfabetico = false
    private var filtroEncerrados = false
    private var filtroAbertos = false

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.configureToolbarFor(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminEventosAdapter(eventos) { tipo, evento ->
            mostrarDialogoConfirmacao(tipo, evento)
        }

        binding.recyclerAdminEventos.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAdminEventos.adapter = adapter

        // filtro
        binding.btnFiltrarEvento.setOnClickListener {
            mostrarDialogoFiltros()
        }

        // NOVO: botão adicionar
        binding.btnAdicionarEvento.setOnClickListener {
            mostrarDialogAdicionar()
        }

        // busca
        binding.etBuscarEvento.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtrados = eventos.filter { it.nome.lowercase().contains(query) }
            adapter.updateData(filtrados)
            // se o seu adapter não for ListAdapter, troca por notifyDataSetChanged()
        }
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

        if (filtroData) filtrados = filtrados.sortedBy { it.data }
        if (filtroAlfabetico) filtrados = filtrados.sortedBy { it.nome }
        if (filtroEncerrados) filtrados = filtrados.filter { it.isEncerrado }
        if (filtroAbertos) filtrados = filtrados.filter { it.isAberto }

        adapter.updateData(filtrados)

    }

    private fun mostrarDialogoConfirmacao(tipo: String, evento: AdminEvento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirma sua ação")
            .setMessage("Deseja realmente alterar o estado de: $tipo\nEvento: ${evento.nome}?")
            .setPositiveButton("SIM") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("NÃO") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // NOVO: diálogo para adicionar evento
    private fun mostrarDialogAdicionar() {
        val dlgView = layoutInflater.inflate(R.layout.dialog_admin_evento, null)
        val edtTitulo = dlgView.findViewById<android.widget.EditText>(R.id.edtTitulo)
        val edtLocal = dlgView.findViewById<android.widget.EditText>(R.id.edtLocal)
        val edtVagas = dlgView.findViewById<android.widget.EditText>(R.id.edtVagas)
        val edtData = dlgView.findViewById<android.widget.EditText>(R.id.edtData)
        val edtHorario = dlgView.findViewById<android.widget.EditText>(R.id.edtHorario)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo evento")
            .setView(dlgView)
            .setPositiveButton("Salvar") { d, _ ->
                val novo = AdminEvento(
                    nome = edtTitulo.text.toString().ifBlank { "Evento sem nome" },
                    local = edtLocal.text.toString(),
                    vagas = edtVagas.text.toString().toIntOrNull() ?: 0,
                    data = edtData.text.toString(),
                    horario = edtHorario.text.toString()
                )
                // adiciona no início
                eventos.add(0, novo)
                adapter.updateData(eventos.toList())
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
