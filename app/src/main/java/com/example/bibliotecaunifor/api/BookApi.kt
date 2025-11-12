package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.CreateBookDto
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.models.Rental
import retrofit2.Call
import retrofit2.http.*

interface BookApi {

    // ====== USUÁRIO ======

    @GET("books")
    fun getBooks(@Header("Authorization") token: String): Call<List<Book>>
    @GET("books/{id}")
    fun getBookById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<Book>
    @POST("books/{id}/rent")
    fun rentBook(
        @Path("id") bookId: String,
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>

    @POST("books/{id}/return")
    fun returnBook(
        @Path("id") bookId: String,
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>

    @GET("books/my-rentals")
    fun getMyRentals(@Header("Authorization") token: String): Call<List<Rental>>

    // ====== ADMIN ======

    @POST("books")
    fun createBook(
        @Body dto: CreateBookDto,
        @Header("Authorization") token: String
    ): Call<Book>

    @PATCH("books/{id}")
    fun updateBook(
        @Path("id") bookId: String,
        @Body dto: EditBookDto,
        @Header("Authorization") token: String
    ): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(
        @Path("id") bookId: String,
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>
}