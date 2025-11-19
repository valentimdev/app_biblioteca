package com.example.bibliotecaunifor.models

data class Mensagem(
    val text: String,
    val isSentByUser: Boolean,
    val isTyping: Boolean = false
)