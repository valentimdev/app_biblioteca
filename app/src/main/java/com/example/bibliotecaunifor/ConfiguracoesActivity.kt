package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.databinding.ActivityConfiguracoesBinding
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.button.MaterialButton

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 🔹 Botão de regulamento
        binding.tvRegulamento.setOnClickListener {
            showRegulamentoDialog()
        }

        // 🔹 Botão de alterar senha
        binding.tvAlterarSenha.setOnClickListener {
            showAlterarSenhaDialog()
        }

        // 🔹 Botão de sair (logout)
        binding.tvSair.setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showRegulamentoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Regulamento da Biblioteca")
            .setMessage(
                "BLABLABLABLABLABLABLA\n" +
                        "BLABLABLABLABLABLABLA\n" +
                        "BLABLABLABLABLABLABLA"
            )
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
            // futuramente lógica de alterar a senha será aqui
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sair da conta")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("SAIR") { _, _ ->
                logoutAndGoToLogin()
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun logoutAndGoToLogin() {
        AuthUtils.clear(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
