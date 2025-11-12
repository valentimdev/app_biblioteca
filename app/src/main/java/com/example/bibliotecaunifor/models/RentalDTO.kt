package com.example.bibliotecaunifor.models

import com.example.bibliotecaunifor.Book

data class RentalDTO(
    val book: Book,
    val dueDate: String, // ISO: "2025-11-20T00:00:00Z"
    val returnDate: String?
)