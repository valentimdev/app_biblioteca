package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.models.BookStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.jvm.JvmSuppressWildcards

interface BookApi {

    // ================== LISTAR / DETALHE ==================

    // Lista pública (aluno/usuário)
    @GET("books")
    fun getBooks(): Call<List<Book>>

    // Lista completa (ADMIN) – vamos usar no CatalogAdminFragment
    @GET("books/admin")
    fun getAdminBooks(): Call<List<Book>>

    @GET("books/{id}")
    fun getBook(@Path("id") id: String): Call<Book>

    // ✅ Agora batendo com o backend: retorna BookStatus (com isRentedByUser, rentalInfo etc.)
    @GET("books/{id}/status")
    fun getBookStatus(@Path("id") id: String): Call<BookStatus>

    // ================== CRUD ADMIN (multipart) ==================

    @Multipart
    @POST("books")
    fun createBook(
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("isbn") isbn: RequestBody,
        @Part("description") description: RequestBody,
        @Part("totalCopies") totalCopies: RequestBody,
        @Part("availableCopies") availableCopies: RequestBody,
        @Part image: MultipartBody.Part? = null,
        @Part("imageUrl") imageUrl: RequestBody? = null
    ): Call<Book>

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
        @Part image: MultipartBody.Part? = null,
        @Part("imageUrl") imageUrl: RequestBody? = null
    ): Call<Book>

    // ✅ PATCH /books/{id}/flags – usado nos toggles do admin
    @PATCH("books/{id}/flags")
    fun patchBookFlags(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(@Path("id") id: String): Call<Map<String, Boolean>>

    // ================== EMPRÉSTIMOS ==================

    @POST("books/{id}/rent")
    fun rentBookWithDueDate(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Boolean>>

    @POST("books/{id}/return")
    fun returnBook(
        @Path("id") id: String
    ): Call<Map<String, Boolean>>

    @GET("rentals/my")
    fun getMyRentals(): Call<List<Rental>>
}
