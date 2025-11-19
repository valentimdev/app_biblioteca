package com.example.bibliotecaunifor.admin

data class User(
    val id: String,
    val name: String,
    val email: String,
    val matricula: String,
    val role: String,
    val imageUrl: String? = null
)