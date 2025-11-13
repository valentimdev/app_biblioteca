package com.example.bibliotecaunifor

import com.google.gson.annotations.SerializedName

data class Evento(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val imageUrl: String?,
    val lecturers: String?,
    val seats: Int,
    val isDisabled: Boolean,
    val adminId: String,
    val createdAt: String,
    val updatedAt: String
)