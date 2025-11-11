package com.example.bibliotecaunifor.models

data class CreateEventRequest(
    val title: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val lecturers: String? = null,
    val isDisabled: Boolean = false,
    val seats: Int? = null
)