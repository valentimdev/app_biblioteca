package com.example.bibliotecaunifor

data class AdminEvento(
    val id: String? = null,
    val nome: String,
    val local: String,
    val vagas: Int,
    val data: String,
    val horario: String,
    val endTime: String?,
    val ativo: Boolean,
    val description: String? = null,
    val imageUrl: String? = null
)