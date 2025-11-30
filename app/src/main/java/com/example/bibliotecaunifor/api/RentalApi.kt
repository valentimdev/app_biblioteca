package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.models.RenewRentalRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RentalApi {

    // Lista todos os empréstimos de um usuário
    @GET("rentals/user/{userId}")
    fun getByUser(
        @Path("userId") userId: String
    ): Call<List<Rental>>

    // Lista todos os empréstimos de um livro específico
    @GET("rentals/book/{bookId}")
    fun getByBook(
        @Path("bookId") bookId: String
    ): Call<List<Rental>>

    // Devolver um empréstimo (PATCH /rentals/:id/return)
    @PATCH("rentals/{id}/return")
    fun returnRental(
        @Path("id") id: String
    ): Call<Rental>

    // 🔹 RENOVAR empréstimo (PATCH /rentals/:id/renew)
    @PATCH("rentals/{id}/renew")
    fun renewRental(
        @Path("id") id: String,
        @Body body: RenewRentalRequest
    ): Call<Rental>

    // Criar um novo empréstimo (POST /rentals)
    // body deve ter: userId, bookId e opcionalmente dueDate (string ISO)
    @POST("rentals")
    fun rentBook(
        @Body body: Map<String, String>
    ): Call<Rental>
}
