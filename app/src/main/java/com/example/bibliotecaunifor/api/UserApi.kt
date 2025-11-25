package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.models.DashboardResponse
import com.example.bibliotecaunifor.models.EditUserRequest
import com.example.bibliotecaunifor.models.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface UserApi {

    @PATCH("users")
    fun editUser(
        @Body request: EditUserRequest
    ): Call<UserResponse>

    @Multipart
    @PATCH("users")
    fun editUserMultipart(
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<UserResponse>

    @GET("users")
    fun getAllUsers(@Header("Authorization") token: String): Call<List<UserResponse>>

    @GET("users/me")
    fun getMe(): Call<UserResponse>

    @PATCH("users/{id}/toggle-status")
    fun toggleStatus(@Path("id") id: String): Call<UserResponse>

    @GET("users/dashboard")
    fun getDashboardStats(
        
        @Header("Authorization") token: String
    ): Call<DashboardResponse>
}
