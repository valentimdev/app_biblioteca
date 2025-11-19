package com.example.bibliotecaunifor

data class AdminEvento(
    val id: String?,
    val title: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val imageUrl: String?,
    var lecturers: String?,
    var seats: Int,
    val isDisabled: Boolean,
    val adminId: String,
    val createdAt: String,
    val updatedAt: String
)

