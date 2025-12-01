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

    // 🔹 novos campos vindos do backend
    val isHidden: Boolean = false,      // se true, aluno não deve ver
    val loanEnabled: Boolean = true,    // se false, não pode alugar

    // já existia – usado só no app
    var isRentedByUser: Boolean = false
) : Parcelable
