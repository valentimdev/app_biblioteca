package com.example.bibliotecaunifor.admin

enum class UserStatus { ATIVO, BLOQUEADO }

data class User(
    val id: String,
    val nome: String,
    val matricula: String,
    val status: UserStatus
)