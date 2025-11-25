package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.models.Rental
import okhttp3.MultipartBody
import okhttp3.RequestBody
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


    @Multipart
    @POST("books")
    fun createBook(
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("isbn") isbn: RequestBody,
        @Part("description") description: RequestBody,
        @Part("totalCopies") totalCopies: RequestBody,
        @Part("availableCopies") availableCopies: RequestBody,
        @Part image: MultipartBody.Part?,
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
