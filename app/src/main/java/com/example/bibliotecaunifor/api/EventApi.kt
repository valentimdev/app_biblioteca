package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Evento
import retrofit2.Call
import retrofit2.http.*

interface EventApi {

    // Usado no calendário e lista de eventos do usuário
    @GET("events")
    fun getAllEvents(): Call<List<Evento>>

    // Inscrição (usuário comum)
    @POST("events/{id}/register")
    fun register(@Path("id") eventId: String): Call<Void>

    // Cancelar inscrição
    @DELETE("events/{id}/register")
    fun unregister(@Path("id") eventId: String): Call<Void>

    // Para admin: atualizar evento (seats, lecturers, etc)
    @PATCH("events/{id}")
    fun updateEvent(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Call<Void>

    // Para admin: toggle isDisabled
    @PATCH("events/{id}")
    fun toggleEventStatus(
        @Path("id") id: String,
        @Body body: Map<String, Boolean>
    ): Call<Void>
}