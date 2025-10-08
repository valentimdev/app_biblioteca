package com.example.bibliotecaunifor

import java.util.Calendar

data class Evento(
    val titulo: String,
    val data: Calendar,
    val descricao: String,
    var inscrito: Boolean = false
)