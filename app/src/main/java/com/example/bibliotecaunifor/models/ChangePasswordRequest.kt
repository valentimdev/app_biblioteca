package com.example.bibliotecaunifor.models

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
