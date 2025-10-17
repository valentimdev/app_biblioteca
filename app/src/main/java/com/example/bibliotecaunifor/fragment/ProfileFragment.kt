package com.example.bibliotecaunifor.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.ConfiguracoesActivity
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.NotificacoesActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.EventoCarrosselAdapter
import com.example.bibliotecaunifor.adapters.LivroAdapter
import java.util.Calendar

class ProfileFragment : Fragment(R.layout.activity_perfil_usuario) {

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("PERFIL", showBack = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val buttonEditarPerfil = view.findViewById<ImageButton>(R.id.buttonEditarPerfil)
        buttonEditarPerfil.setOnClickListener { mostrarPopupEditarPerfil(view) }

        val recyclerLivros = view.findViewById<RecyclerView>(R.id.recyclerLivros)
        val recyclerEventos = view.findViewById<RecyclerView>(R.id.recyclerEventos)

        val livros = listOf("Dom Casmurro", "O Hobbit", "1984", "O Pequeno Príncipe", "O Alquimista")
        val eventos = listOf(
            Evento("Feira do Livro", Calendar.getInstance(), "Descrição do evento"),
            Evento("Semana da Leitura", Calendar.getInstance(), "Descrição do evento"),
            Evento("Sarau de Poesia", Calendar.getInstance(), "Descrição do evento"),
            Evento("Encontro Literário", Calendar.getInstance(), "Descrição do evento")
        )

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerLivros.adapter = LivroAdapter(livros) { livro ->
            mostrarDialogLivro(livro)
        }

        recyclerEventos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerEventos.adapter = EventoCarrosselAdapter(eventos) { evento ->
            mostrarDialogEvento(evento)
        }

        val buttonConfiguracoes = view.findViewById<ImageButton>(R.id.buttonConfiguracoes)
        val buttonNotificacoes = view.findViewById<ImageButton>(R.id.buttonNotificacoes)

        buttonConfiguracoes.setOnClickListener {
            val intent = Intent(requireContext(), ConfiguracoesActivity::class.java)
            startActivity(intent)
        }

        buttonNotificacoes.setOnClickListener {
            val intent = Intent(requireContext(), NotificacoesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun mostrarDialogLivro(livro: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_info_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.attributes = layoutParams

        val textTitulo = dialog.findViewById<TextView>(R.id.textTituloLivro)
        val textDescricao = dialog.findViewById<TextView>(R.id.textDescricaoLivro)
        val textDataEmprestado = dialog.findViewById<TextView>(R.id.textDataEmprestado)
        val textDataDevolucao = dialog.findViewById<TextView>(R.id.textDataDevolucao)
        val buttonFechar = dialog.findViewById<Button>(R.id.buttonFechar)

        textTitulo.text = livro

        when (livro) {
            "Dom Casmurro" -> {
                textDescricao.text = "Romance clássico de Machado de Assis."
                textDataEmprestado.text = "Emprestado: 01/10/2025"
                textDataDevolucao.text = "Devolução: 10/10/2025"
            }
            "O Hobbit" -> {
                textDescricao.text = "Aventura fantástica de J.R.R. Tolkien."
                textDataEmprestado.text = "Emprestado: 02/10/2025"
                textDataDevolucao.text = "Devolução: 12/10/2025"
            }
            "1984" -> {
                textDescricao.text = "Distopia clássica de George Orwell."
                textDataEmprestado.text = "Emprestado: 05/10/2025"
                textDataDevolucao.text = "Devolução: 15/10/2025"
            }
            "O Pequeno Príncipe" -> {
                textDescricao.text = "Obra poética de Antoine de Saint-Exupéry."
                textDataEmprestado.text = "Emprestado: 06/10/2025"
                textDataDevolucao.text = "Devolução: 16/10/2025"
            }
            "O Alquimista" -> {
                textDescricao.text = "Fábula filosófica de Paulo Coelho."
                textDataEmprestado.text = "Emprestado: 07/10/2025"
                textDataDevolucao.text = "Devolução: 17/10/2025"
            }
        }

        buttonFechar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun mostrarDialogEvento(evento: Evento) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_info_evento)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        // Largura maior
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.attributes = layoutParams

        val textTitulo = dialog.findViewById<TextView>(R.id.tvTituloEvento)
        val textDescricao = dialog.findViewById<TextView>(R.id.tvDescricaoEvento)
        val textDataHora = dialog.findViewById<TextView>(R.id.tvDataHoraEvento)
        val buttonFechar = dialog.findViewById<Button>(R.id.buttonFecharEvento)
        val btnInscrever = dialog.findViewById<Button>(R.id.btnInscreverEvento)

        textTitulo.text = evento.titulo
        textDescricao.text = evento.descricao
        textDataHora.text = evento.data.time.toString() // formate como quiser

        // Remove o botão de inscrição para eventos do perfil
        btnInscrever.visibility = View.GONE

        buttonFechar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun mostrarPopupEditarPerfil(view: View) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_editar_perfil)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.attributes = layoutParams

        val editUsername = dialog.findViewById<EditText>(R.id.editUsername)
        val buttonSalvar = dialog.findViewById<Button>(R.id.buttonSalvar)
        val buttonMudarFoto = dialog.findViewById<ImageButton>(R.id.buttonMudarFoto)

        val textViewUsername = view.findViewById<TextView>(R.id.textViewUsername)
        editUsername.setText(textViewUsername.text)

        buttonMudarFoto.setOnClickListener {}

        buttonSalvar.setOnClickListener {
            textViewUsername.text = editUsername.text.toString()
            dialog.dismiss()
        }

        dialog.show()
    }
}
