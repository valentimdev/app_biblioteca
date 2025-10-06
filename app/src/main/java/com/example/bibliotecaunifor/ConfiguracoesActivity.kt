package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.ActivityConfiguracoesBinding // A importação correta
import com.google.android.material.button.MaterialButton


class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegulamento.setOnClickListener {
            showRegulamentoDialog()
        }

        binding.tvAlterarSenha.setOnClickListener {
            showAlterarSenhaDialog()
        }
//        binding.voltarButton.setOnClickListener {
//            finish() // Fecha a tela atual e volta para a anterior
//        }


    }


    private fun showRegulamentoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Regulamento da Biblioteca")
            .setMessage("Aqui será exibido o texto completo do regulamento da biblioteca. " +
                    "Este é um texto de exemplo para preencher o espaço enquanto a lógica " +
                    "não é implementada.")
            .setPositiveButton("FECHAR") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun showAlterarSenhaDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alterar_senha, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()
        val btnAlterar = dialogView.findViewById<MaterialButton>(R.id.btnAlterar)

        btnAlterar.setOnClickListener {
            // futuramente logica de alterar a senha via ser aq

            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

}

