package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
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
import android.view.View
import android.widget.TextView
class LoginActivity : AppCompatActivity() {

    private lateinit var matriculaEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var adminButton: Button
    private lateinit var cadastroLinkTextView: TextView
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
        cadastroLinkTextView = findViewById(R.id.cadastroLinkTextView)
        cadastrarTextView = findViewById(R.id.cadastrar)
        toolbar = findViewById(R.id.toolbar)
        blockedUserMessage = findViewById(R.id.blockedUserMessage)
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
            val matricula = matriculaEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (matricula.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                // 1. Tenta login normal (não muda nada)
                val (token, role) = NetworkHelper.login(matricula, senha)

                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true

                    if (token != null && role != null) {
                        // Sucesso normal
                        AuthUtils.saveToken(this@LoginActivity, token)
                        RetrofitClient.setToken(token)
                        Toast.makeText(this@LoginActivity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                        val intent = if (role.equals("ADMIN", ignoreCase = true)) {
                            Intent(this@LoginActivity, AdminActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, MainActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // Falha: ativa a gambiarra pra checar status
                        CoroutineScope(Dispatchers.IO).launch {
                            val status = NetworkHelper.checkStatusFromError(matricula)  // <- AQUI A GAMBIA!

                            withContext(Dispatchers.Main) {
                                if (status == UserStatus.BANNED) {
                                    // Usuário existe mas tá banido
                                    blockedUserMessage?.visibility = View.VISIBLE
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Usuário bloqueado. Vá à biblioteca para desbloquear.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    // Verdadeiro erro de credenciais (não existe ou senha errada)
                                    Toast.makeText(this@LoginActivity, "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        cadastroLinkTextView.setOnClickListener {
            startActivity(Intent(this, EsqueceuSenhaActivity::class.java))
        }

        cadastrarTextView.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}
