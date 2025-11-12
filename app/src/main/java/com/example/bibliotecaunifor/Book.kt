package com.example.bibliotecaunifor

import com.google.gson.annotations.SerializedName

data class Book(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val title: String,
    val author: String,
    val isbn: String?,
    val description: String?,
    val totalCopies: Int,
    val availableCopies: Int,
    val adminId: String
)