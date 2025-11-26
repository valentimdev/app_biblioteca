package com.example.bibliotecaunifor.notifications

data class AppNotification(
    val id: Long,
    val title: String,
    val message: String,
    val dateMillis: Long,
    val type: String? = null,
    val read: Boolean = false
)
