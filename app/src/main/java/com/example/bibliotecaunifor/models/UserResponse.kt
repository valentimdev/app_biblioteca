package com.example.bibliotecaunifor.models

import com.example.bibliotecaunifor.admin.UserStatus

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val matricula: String,
    val imageUrl: String?,
    val role: String,
    val status: UserStatus?,
    val rentals: List<Rental> = emptyList(),
    val events: List<EventDto> = emptyList(),
)
