package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var welcomeTextView: TextView
    private lateinit var gerenciarUsuariosButton: Button
    private lateinit var gerenciarLivrosButton: Button
    private lateinit var relatoriosButton: Button
    private lateinit var configuracoesButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        gerenciarUsuariosButton = findViewById(R.id.gerenciarUsuariosButton)
        gerenciarLivrosButton = findViewById(R.id.gerenciarLivrosButton)
        relatoriosButton = findViewById(R.id.relatoriosButton)
        configuracoesButton = findViewById(R.id.configuracoesButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.admin_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupClickListeners() {
        gerenciarUsuariosButton.setOnClickListener {
            // TODO: Implementar tela de gerenciamento de usuários
            Toast.makeText(this, "${getString(R.string.admin_manage_users)} - ${getString(R.string.admin_coming_soon)}", Toast.LENGTH_SHORT).show()
        }

        gerenciarLivrosButton.setOnClickListener {
            // TODO: Implementar tela de gerenciamento de livros
            Toast.makeText(this, "${getString(R.string.admin_manage_books)} - ${getString(R.string.admin_coming_soon)}", Toast.LENGTH_SHORT).show()
        }

        relatoriosButton.setOnClickListener {
            // TODO: Implementar tela de relatórios
            Toast.makeText(this, "${getString(R.string.admin_reports)} - ${getString(R.string.admin_coming_soon)}", Toast.LENGTH_SHORT).show()
        }

        configuracoesButton.setOnClickListener {
            val intent = Intent(this, ConfiguracoesActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        // Limpar dados de sessão se necessário
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
