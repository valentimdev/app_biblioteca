package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.SignupRequest
import com.example.bibliotecaunifor.models.SignupResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var matriculaEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var confirmarSenhaEditText: EditText
    private lateinit var cadastrarButton: Button
    private lateinit var loginLinkTextView: TextView
    private lateinit var voltarButton2: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        nomeEditText = findViewById(R.id.nomeEditText)
        emailEditText = findViewById(R.id.emailEditText)
        matriculaEditText = findViewById(R.id.matriculaEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLinkTextView = findViewById(R.id.loginLinkTextView)
        voltarButton2 = findViewById(R.id.voltarButton2)

        voltarButton2.setOnClickListener { finish() }

        loginLinkTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        cadastrarButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val matricula = matriculaEditText.text.toString().trim()
            val senha = senhaEditText.text.toString()
            val confirmarSenha = confirmarSenhaEditText.text.toString()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cadastrarUsuario(nome, email, matricula, senha)
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, matricula: String, senha: String) {
        val request = SignupRequest(
            matricula = matricula,
            email = email,
            password = senha,
            nome = nome
        )

        RetrofitClient.authApi.signup(request).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@CadastroActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(this@CadastroActivity, "Erro no cadastro: $error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Toast.makeText(this@CadastroActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
