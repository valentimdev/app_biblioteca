package com.example.bibliotecaunifor

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.LivroAdapter
import com.example.bibliotecaunifor.adapters.EventoAdapter

class PerfilUsuario: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        // Botão de editar perfil
        val buttonEditarPerfil = findViewById<ImageButton>(R.id.buttonEditarPerfil)
        buttonEditarPerfil.setOnClickListener { mostrarPopupEditarPerfil() }

        // RecyclerViews
        val recyclerLivros = findViewById<RecyclerView>(R.id.recyclerLivros)
        val recyclerEventos = findViewById<RecyclerView>(R.id.recyclerEventos)

        // Dados hardcoded
        val livros = listOf("Dom Casmurro", "O Hobbit", "1984", "O Pequeno Príncipe", "O Alquimista")
        val eventos = listOf("Feira do Livro", "Semana da Leitura", "Sarau de Poesia", "Encontro Literário")

        // Configurar RecyclerView de Livros
        recyclerLivros.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerLivros.adapter = LivroAdapter(livros) { livro ->
            mostrarDialogLivro(livro)
        }

        // Configurar RecyclerView de Eventos
        recyclerEventos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerEventos.adapter = EventoAdapter(eventos) { evento ->
            mostrarDialogEvento(evento)
        }

        val buttonConfiguracoes = findViewById<ImageButton>(R.id.buttonConfiguracoes)
        val buttonNotificacoes = findViewById<ImageButton>(R.id.buttonNotificacoes)

        buttonConfiguracoes.setOnClickListener {
            val intent = Intent(this, ConfiguracoesActivity::class.java)
            startActivity(intent)
        }

        buttonNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            startActivity(intent)
        }

    }

    private fun mostrarDialogLivro(livro: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val textTitulo = dialog.findViewById<TextView>(R.id.textTituloLivro)
        textTitulo.text = livro

        val buttonFechar = dialog.findViewById<Button>(R.id.buttonFechar)
        buttonFechar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogEvento(evento: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info_evento)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val textTitulo = dialog.findViewById<TextView>(R.id.textTituloEvento)
        textTitulo.text = evento

        val buttonFechar = dialog.findViewById<Button>(R.id.buttonFecharEvento)
        buttonFechar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarPopupEditarPerfil() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_perfil)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Aumentar largura do dialog
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% da tela
        dialog.window?.attributes = layoutParams

        val editUsername = dialog.findViewById<EditText>(R.id.editUsername)
        val buttonSalvar = dialog.findViewById<Button>(R.id.buttonSalvar)
        val buttonMudarFoto = dialog.findViewById<ImageButton>(R.id.buttonMudarFoto)

        val textViewUsername = findViewById<TextView>(R.id.textViewUsername)
        editUsername.setText(textViewUsername.text)

        buttonMudarFoto.setOnClickListener {
            // Implementar mudança de foto depois
        }

        buttonSalvar.setOnClickListener {
            textViewUsername.text = editUsername.text.toString()
            dialog.dismiss()
        }

        dialog.show()
    }
}
