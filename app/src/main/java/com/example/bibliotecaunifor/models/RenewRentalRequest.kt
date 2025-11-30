package com.example.bibliotecaunifor.models

data class RenewRentalRequest(
    val additionalDays: Int? = null // se null, o back usa 7 dias padrão
)
