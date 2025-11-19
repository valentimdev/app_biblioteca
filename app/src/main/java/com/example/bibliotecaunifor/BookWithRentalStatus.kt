package com.example.bibliotecaunifor

data class BookWithRentalStatus(
    val book: Book,
    val isRentedByUser: Boolean
)