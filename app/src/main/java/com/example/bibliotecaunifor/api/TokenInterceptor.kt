package com.example.bibliotecaunifor.api

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val req = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            req.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(req.build())
    }
}