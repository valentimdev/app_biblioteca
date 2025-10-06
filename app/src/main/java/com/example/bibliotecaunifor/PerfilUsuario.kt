package com.example.bibliotecaunifor

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilUsuario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val containerLivros = findViewById<LinearLayout>(R.id.containerLivros)
        val containerEventos = findViewById<LinearLayout>(R.id.containerEventos)

        val livros = listOf("Livro A", "Livro B", "Livro C", "Livro D", "Livro E")
        val eventos = listOf("Evento 1", "Evento 2", "Evento 3", "Evento 4", "Evento 5")

        adicionarItensCarrossel(containerLivros, livros)
        adicionarItensCarrossel(containerEventos, eventos)

        val buttonEditarPerfil = findViewById<ImageButton>(R.id.buttonEditarPerfil)
        buttonEditarPerfil.setOnClickListener {
            mostrarPopupEditarPerfil()
        }
    }

    private fun adicionarItensCarrossel(container: LinearLayout, itens: List<String>) {
        for (titulo in itens) {
            val itemView = layoutInflater.inflate(R.layout.activity_item_carrossel, container, false)

            val image = itemView.findViewById<ImageView>(R.id.imageItem)
            val text = itemView.findViewById<TextView>(R.id.textItemTitle)

            text.text = titulo
            image.setImageResource(R.drawable.ic_launcher_background)

            itemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            container.addView(itemView)
        }
    }

    private fun mostrarPopupEditarPerfil() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_perfil)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val imagePerfil = dialog.findViewById<ImageView>(R.id.imagePerfilPopup)
        val buttonMudarFoto = dialog.findViewById<ImageButton>(R.id.buttonMudarFoto)
        val editUsername = dialog.findViewById<EditText>(R.id.editUsername)
        val buttonSalvar = dialog.findViewById<Button>(R.id.buttonSalvar)

        editUsername.setText(findViewById<TextView>(R.id.textViewUsername).text)

        buttonMudarFoto.setOnClickListener {
        }

        buttonSalvar.setOnClickListener {
            findViewById<TextView>(R.id.textViewUsername).text = editUsername.text.toString()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
    }
}