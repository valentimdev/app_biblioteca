package com.example.bibliotecaunifor.models

data class EditBookDto(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String?,
    val totalCopies: Int?
)