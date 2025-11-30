package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.Rental
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BookApi {

    @GET("books")
    fun getBooks(): Call<List<Book>>

    @GET("books/{id}")
    fun getBook(@Path("id") id: String): Call<Book>

    // Criar livro com ou sem imagem
    @Multipart
    @POST("books")
    fun createBook(
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("isbn") isbn: RequestBody,
        @Part("description") description: RequestBody,
        @Part("totalCopies") totalCopies: RequestBody,
        @Part("availableCopies") availableCopies: RequestBody,
        @Part image: MultipartBody.Part? = null,              // arquivo opcional
        @Part("imageUrl") imageUrl: RequestBody? = null       // URL opcional (se não tiver arquivo)
    ): Call<Book>

    // Atualizar livro (PATCH) com ou sem imagem nova
    @Multipart
    @PATCH("books/{id}")
    fun updateBook(
        @Path("id") id: String,
        @Part("title") title: RequestBody?,
        @Part("author") author: RequestBody?,
        @Part("isbn") isbn: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("totalCopies") totalCopies: RequestBody?,
        @Part("availableCopies") availableCopies: RequestBody?,
        @Part image: MultipartBody.Part? = null,              // arquivo opcional
        @Part("imageUrl") imageUrl: RequestBody? = null       // URL opcional
    ): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(@Path("id") id: String): Call<Map<String, Boolean>>

    // ✅ Alugar livro com data de devolução
    @POST("books/{id}/rent")
    fun rentBookWithDueDate(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Boolean>>

    // ✅ Devolver livro
    @POST("books/{id}/return")
    fun returnBook(
        @Path("id") id: String
    ): Call<Map<String, Boolean>>

    // ✅ Buscar alugueis do usuário logado
    @GET("rentals/my")
    fun getMyRentals(): Call<List<Rental>>
}
