package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.network.NetworkHelper
import com.example.bibliotecaunifor.utils.AuthUtils
import com.example.bibliotecaunifor.admin.AdminActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Se já tiver token salvo, pula direto
        AuthUtils.getToken(this)?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initViews() {
        matriculaEditText = findViewById(R.id.matriculaEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        adminButton = findViewById(R.id.btnAdmin)
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
        loginButton.setOnClickListener {
            val email = matriculaEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val (token, role) = NetworkHelper.login(email, senha)

                withContext(Dispatchers.Main) {
                    if (token != null && role != null) {
                        AuthUtils.saveToken(this@LoginActivity, token)

                        Toast.makeText(this@LoginActivity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                        val nextIntent = if (role.equals("ADMIN", ignoreCase = true)) {
                            Intent(this@LoginActivity, AdminActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, MainActivity::class.java)
                        }

                        startActivity(nextIntent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        adminButton.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        cadastroLinkTextView.setOnClickListener {
            startActivity(Intent(this, EsqueceuSenhaActivity::class.java))
        }

        cadastrarTextView.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}
