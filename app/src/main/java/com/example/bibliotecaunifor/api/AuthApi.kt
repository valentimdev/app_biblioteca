package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.models.LoginRequest
import com.example.bibliotecaunifor.models.LoginResponse
import com.example.bibliotecaunifor.models.SignupRequest
import com.example.bibliotecaunifor.models.SignupResponse
import com.example.bibliotecaunifor.models.ChangePasswordRequest
import com.example.bibliotecaunifor.models.ChangePasswordResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/signup")
    fun signup(@Body body: SignupRequest): Call<SignupResponse>

    @POST("auth/signin")
    fun signin(@Body body: LoginRequest): Call<LoginResponse>

    @POST("auth/change-password")
    fun changePassword(
        @Body body: ChangePasswordRequest
    ): Call<ChangePasswordResponse>
}
