package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.CreateBookDto
import com.example.bibliotecaunifor.models.EditBookDto
import retrofit2.Call
import retrofit2.http.*

interface BookApi {

    @GET("books")
    fun getBooks(@Header("Authorization") token: String): Call<List<Book>>

    @POST("books")
    fun createBook(
        @Body dto: CreateBookDto,
        @Header("Authorization") token: String
    ): Call<Book>

    @PATCH("books/{id}")
    fun updateBook(
        @Path("id") id: String,
        @Body dto: EditBookDto,
        @Header("Authorization") token: String
    ): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>
}
