package com.example.bibliotecaunifor.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    fun getToken(): String? {
        return token
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor { token })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val bookApi: BookApi = retrofit.create(BookApi::class.java)

    val userApi: UserApi = retrofit.create(UserApi::class.java)

    val rentalApi: RentalApi = retrofit.create(RentalApi::class.java)

    // Todos os eventos e inscrições agora aqui (substitui EventService e EventApi antigo)
    val eventApi: EventApi = retrofit.create(EventApi::class.java)
}