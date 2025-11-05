package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.admin.AdminActivity
import com.google.android.material.appbar.MaterialToolbar

class LoginActivity : AppCompatActivity() {

    private lateinit var matriculaEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var adminButton: Button
    private lateinit var cadastroLinkTextView: TextView
    private lateinit var cadastrarTextView: TextView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initViews() {
        matriculaEditText = findViewById(R.id.matriculaEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        adminButton = findViewById(R.id.btnAdmin)   // 👈 novo
        cadastroLinkTextView = findViewById(R.id.cadastroLinkTextView)
        cadastrarTextView = findViewById(R.id.cadastrar)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.login_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupClickListeners() {
        // login normal
        loginButton.setOnClickListener {
            performLogin()
        }

        // atalho para admin (sem validar nada, por enquanto)
        adminButton.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        cadastroLinkTextView.setOnClickListener {
            val intent = Intent(this, EsqueceuSenhaActivity::class.java)
            startActivity(intent)
        }

        cadastrarTextView.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val matricula = matriculaEditText.text.toString().trim()
        val senha = senhaEditText.text.toString().trim()

        if (validateInput(matricula, senha)) {
            // Simulação de login - sem backend
            if (matricula == getString(R.string.login_admin_credentials) &&
                senha == getString(R.string.login_admin_credentials)
            ) {
                // Login como administrador
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
                finish()
            } else if (matricula.isNotEmpty() && senha.isNotEmpty()) {
                // Login como usuário comum
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.login_error_invalid_credentials),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateInput(matricula: String, senha: String): Boolean {
        if (matricula.isEmpty()) {
            matriculaEditText.error = getString(R.string.login_error_matricula_required)
            matriculaEditText.requestFocus()
            return false
        }

        if (senha.isEmpty()) {
            senhaEditText.error = getString(R.string.login_error_senha_required)
            senhaEditText.requestFocus()
            return false
        }

        if (senha.length < 6) {
            senhaEditText.error = getString(R.string.login_error_senha_length)
            senhaEditText.requestFocus()
            return false
        }

        return true
    }
}
