package com.example.bibliotecaunifor

data class Rental(
    val id: String,
    val userId: String,
    val bookId: String,
    val rentalDate: String,
    val dueDate: String,
    val returnDate: String?,
    val book: Book
)