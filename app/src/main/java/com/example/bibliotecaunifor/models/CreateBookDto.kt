package com.example.bibliotecaunifor.models

data class CreateBookDto(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String?,
    val totalCopies: Int
)