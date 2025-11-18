package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.services.EventService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    private val okHttpClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor { token })
            .build()

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    val authApi: AuthApi
        get() = retrofit.create(AuthApi::class.java)

    val eventApi: EventService
        get() = retrofit.create(EventService::class.java)

    val bookApi: BookApi
        get() = retrofit.create(BookApi::class.java)

    val userApi: UserApi
        get() = retrofit.create(UserApi::class.java)
}