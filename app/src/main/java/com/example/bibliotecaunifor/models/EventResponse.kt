package com.example.bibliotecaunifor.models

data class EventResponse(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: String,
    val endTime: String?,
    val location: String,
    val imageUrl: String?,
    val lecturers: String?,
    val seats: Int?,
    val isDisabled: Boolean?,
    val adminId: String?,
    val createdAt: String?,
    val updatedAt: String?
)