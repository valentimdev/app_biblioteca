package com.example.bibliotecaunifor.models

data class SignupRequest(
    val matricula: String,
    val email: String,
    val password: String,
    val nome: String? = null
)