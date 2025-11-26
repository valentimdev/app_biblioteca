package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.models.Rental
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BookApi {

    @GET("books")
    fun getBooks(): Call<List<Book>>

    @GET("books/{id}")
    fun getBookById(@Path("id") id: String): Call<Book>

    // VERSÃO CORRETA: SEM BODY, SEM TOKEN
    @POST("books/{id}/rent")
    fun rentBook(@Path("id") bookId: String): Call<Map<String, Boolean>>

    @POST("books/{id}/return")
    fun returnBook(@Path("id") bookId: String): Call<Map<String, Boolean>>

    @GET("books/my-rentals")
    fun getMyRentals(): Call<List<Rental>>

    // === ADMIN ===
    @Multipart
    @POST("books")
    fun createBook(
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("isbn") isbn: RequestBody,
        @Part("description") description: RequestBody,
        @Part("totalCopies") totalCopies: RequestBody,
        @Part("availableCopies") availableCopies: RequestBody,
        @Part("imageUrl") imageUrl: RequestBody?     // 👈 novo campo
        // pode deixar o @Part image: MultipartBody.Part? se quiser no futuro
    ): Call<Book>


    @PATCH("books/{id}")
    fun updateBook(
        @Path("id") bookId: String,
        @Body dto: EditBookDto
    ): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(@Path("id") bookId: String): Call<Map<String, Boolean>>
    @POST("books/{id}/rent")
    fun rentBookWithDueDate(
        @Path("id") bookId: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Boolean>>
}