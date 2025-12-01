package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.network.NetworkHelper
import com.example.bibliotecaunifor.utils.AuthUtils
import com.example.bibliotecaunifor.admin.AdminActivity
import com.example.bibliotecaunifor.admin.UserStatus
import com.example.bibliotecaunifor.api.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var matriculaEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var cadastrarTextView: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var blockedUserMessage: TextView

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

        // Removido: cadastroLinkTextView (era o "Esqueceu senha")
        cadastrarTextView = findViewById(R.id.cadastrar)

        toolbar = findViewById(R.id.toolbar)
        blockedUserMessage = findViewById(R.id.blockedUserMessage)
    }

    // 🔥 Toolbar sem botão de voltar
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.login_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        toolbar.navigationIcon = null
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val matricula = matriculaEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (matricula.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                val (token, role) = NetworkHelper.login(matricula, senha)

                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true

                    if (token != null && role != null) {

                        AuthUtils.saveToken(this@LoginActivity, token)
                        RetrofitClient.setToken(token)

                        Toast.makeText(
                            this@LoginActivity,
                            "Login bem-sucedido!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = if (role.equals("ADMIN", ignoreCase = true)) {
                            Intent(this@LoginActivity, AdminActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, MainActivity::class.java)
                        }

                        startActivity(intent)
                        finish()

                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val status = NetworkHelper.checkStatusFromError(matricula)

                            withContext(Dispatchers.Main) {
                                if (status == UserStatus.BANNED) {
                                    blockedUserMessage.visibility = View.VISIBLE
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Usuário bloqueado. Vá à biblioteca para desbloquear.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Credenciais inválidas",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        // ❌ Removido: botão/link de EsqueceuSenhaActivity
        // cadastroLinkTextView.setOnClickListener { ... }

        cadastrarTextView.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}
