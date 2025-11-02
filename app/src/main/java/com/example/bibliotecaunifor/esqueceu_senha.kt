package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EsqueceuSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_esqueceu_senha)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_esqueceu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val voltarButton = findViewById<ImageButton>(R.id.voltarButton)
        voltarButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val enviarButton = findViewById<Button>(R.id.enviarButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)

        enviarButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu e-mail.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "E-mail enviado com sucesso!", Toast.LENGTH_LONG).show()
            }
        }
    }
}