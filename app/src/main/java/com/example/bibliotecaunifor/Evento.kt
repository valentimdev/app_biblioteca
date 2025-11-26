package com.example.bibliotecaunifor

import com.google.gson.annotations.SerializedName

data class Evento(
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("eventStartTime") val startTime: String,
    @SerializedName("eventEndTime") val endTime: String?,
    val location: String?,
    val imageUrl: String?,
    var lecturers: String?,
    var seats: Int,

    @SerializedName("registrationStartTime")
    val registrationStartTime: String?,

    @SerializedName("registrationEndTime")
    val registrationEndTime: String?,

    val isDisabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)

