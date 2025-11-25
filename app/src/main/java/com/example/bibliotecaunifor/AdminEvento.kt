package com.example.bibliotecaunifor

data class AdminEvento(
    val id: String,
    val title: String,
    val description: String?,
    val registrationStartTime: String?,
    val registrationEndTime: String?,
    val eventStartTime: String,
    val eventEndTime: String?,
    val startTime: String?,
    val endTime: String?,
    val location: String?,
    val imageUrl: String?,
    val lecturers: String?,
    val seats: Int,
    val isDisabled: Boolean,
    val isFull: Boolean,
    val adminId: String?,
    val createdAt: String?,
    val updatedAt: String?
)