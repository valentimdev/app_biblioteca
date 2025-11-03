package com.example.bibliotecaunifor

data class Book(
    val id: String,
    var nome: String,
    var autor: String,
    var oculto: Boolean = false,
    var emprestimoHabilitado: Boolean = true
)