package com.example.bibliotecaunifor.models

data class EventDto(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val startTime: String,
    val endTime: String,
    val location: String,
    val imageUrl: String? = null,
    val lecturers: String? = null,
    val seats: Int? = null,
    val isDisabled: Boolean = false,
)
