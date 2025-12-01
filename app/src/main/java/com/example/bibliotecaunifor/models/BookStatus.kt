package com.example.bibliotecaunifor.models

data class RentalInfo(
    val rentalDate: String,
    val dueDate: String,
    val isOverdue: Boolean
)

data class BookStatus(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String?,
    val description: String?,
    val imageUrl: String?,
    val totalCopies: Int,
    val availableCopies: Int,
    val adminId: String?,
    val isHidden: Boolean,
    val loanEnabled: Boolean,
    val isRentedByUser: Boolean,
    val rentalInfo: RentalInfo?
)
