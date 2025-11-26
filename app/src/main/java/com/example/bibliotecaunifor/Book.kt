package com.example.bibliotecaunifor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val title: String,
    val author: String,
    val isbn: String? = null,
    val description: String? = null,
    val totalCopies: Int,
    val availableCopies: Int,
    val imageUrl: String? = null,
    var isRentedByUser: Boolean = false
) : Parcelable