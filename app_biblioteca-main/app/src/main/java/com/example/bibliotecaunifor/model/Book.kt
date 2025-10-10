package com.example.bibliotecaunifor.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val year: Int,
    val quantity: Int,
    val synopsis: String
)
