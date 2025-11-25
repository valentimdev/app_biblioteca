package com.example.bibliotecaunifor.models

data class DashboardResponse(
    val totalUsers: Int,
    val totalBooks: Int,
    val totalRentedBooks: Int,
    val availableBooks: Int,
    val topRentedBooks: List<TopBook>
)

data class TopBook(
    val id: String,
    val title: String,
    val author: String,
    val imageUrl: String?,
    val isbn: String?,
    val totalRentals: Int
)