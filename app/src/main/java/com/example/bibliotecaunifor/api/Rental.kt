package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.models.Rental
import retrofit2.Call
import retrofit2.http.*

interface RentalApi {
    @GET("rentals/user/{userId}")
    fun getByUser(@Path("userId") userId: String, @Header("Authorization") token: String): Call<List<Rental>>

    @GET("rentals/book/{bookId}")
    fun getByBook(@Path("bookId") bookId: String, @Header("Authorization") token: String): Call<List<Rental>>

    @PATCH("rentals/{id}/return")
    fun returnRental(@Path("id") id: String, @Header("Authorization") token: String): Call<Map<String, Boolean>>

    @POST("rentals")
    fun rentBook(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Call<Rental>
}