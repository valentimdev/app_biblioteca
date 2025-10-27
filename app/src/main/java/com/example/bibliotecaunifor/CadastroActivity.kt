package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Aplicar padding automático (mantém o estilo do seu projeto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_chat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referências dos elementos
        val voltarButton = findViewById<ImageButton>(R.id.voltarButton2)
        val nomeEditText = findViewById<EditText>(R.id.nomeEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val matriculaEditText = findViewById<EditText>(R.id.matriculaEditText)
        val senhaEditText = findViewById<EditText>(R.id.senhaEditText)
        val confirmarSenhaEditText = findViewById<EditText>(R.id.confirmarSenhaEditText)
        val cadastrarButton = findViewById<Button>(R.id.cadastrarButton)
        val loginLinkTextView = findViewById<TextView>(R.id.loginLinkTextView)

        // Botão de voltar
        voltarButton.setOnClickListener {
            finish()
        }

        // Link para login
        loginLinkTextView.setOnClickListener {
            finish()
        }

        // Lógica de cadastro
        cadastrarButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val matricula = matriculaEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()
            val confirmarSenha = confirmarSenhaEditText.text.toString().trim()

            when {
                nome.isEmpty() || email.isEmpty() || matricula.isEmpty() ||
                        senha.isEmpty() || confirmarSenha.isEmpty() -> {
                    Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }

                senha.length < 6 -> {
                    Toast.makeText(
                        this,
                        "A senha deve conter pelo menos 6 caracteres.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                senha != confirmarSenha -> {
                    Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                    // Ir para a tela principal (HomeFragment é carregado dentro da MainActivity)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
