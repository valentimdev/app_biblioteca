package com.example.bibliotecaunifor.services

import com.example.bibliotecaunifor.api.ApiConfig
import com.example.bibliotecaunifor.models.EventDto
import com.example.bibliotecaunifor.AdminEvento
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object EventService {

    private val EVENTS_URL = "${ApiConfig.BASE_URL}events"

    fun getAllEvents(token: String?): List<EventDto> {
        val eventos = mutableListOf<EventDto>()
        val url = URL(EVENTS_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        if (conn.responseCode in 200..299) {
            val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                eventos.add(EventDto(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    description = obj.optString("description"),
                    startTime = obj.optString("startTime"),
                    endTime = obj.optString("endTime"),
                    location = obj.optString("location"),
                    imageUrl = obj.optString("image_url"),
                    lecturers = obj.optString("lecturers")
                ))
            }
        }
        conn.disconnect()
        return eventos
    }

    fun createEvent(token: String?, evento: AdminEvento): AdminEvento {
        val url = URL(EVENTS_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        val json = JSONObject().apply {
            put("title", evento.nome)
            put("location", evento.local)
            put("seats", evento.vagas)
            put("startTime", evento.data)
            put("endTime", evento.endTime)
            put("isDisabled", !evento.ativo)
            put("description", evento.description ?: "")
            put("image_url", evento.imageUrl ?: "")
        }

        conn.outputStream.use { it.write(json.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream.bufferedReader().use { it.readText() }
            throw IOException("Erro ao criar evento: ${conn.responseCode} $err")
        }

        val resp = conn.inputStream.bufferedReader().use { it.readText() }
        val respJson = JSONObject(resp)

        return AdminEvento(
            id = respJson.getString("id"),
            nome = respJson.getString("title"),
            local = respJson.getString("location"),
            vagas = respJson.getInt("seats"),
            data = respJson.getString("startTime"),
            endTime = respJson.getString("endTime"),
            horario = respJson.getString("startTime"),
            ativo = !respJson.getBoolean("isDisabled"),
            description = respJson.optString("description", null),
            imageUrl = respJson.optString("image_url", null)
        )
    }

    fun deleteEvent(token: String?, eventId: String) {
        val url = URL("$EVENTS_URL/$eventId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        if (conn.responseCode !in 200..299) {
            val err = BufferedReader(InputStreamReader(conn.errorStream)).use { it.readText() }
            throw IOException("Erro ao deletar evento: ${conn.responseCode} $err")
        }

        conn.disconnect()
    }

    fun updateEvent(token: String?, eventId: String, dto: JSONObject) {
        val url = URL("$EVENTS_URL/$eventId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        conn.outputStream.use { it.write(dto.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            val err = BufferedReader(InputStreamReader(conn.errorStream)).use { it.readText() }
            throw IOException("Erro ao atualizar evento: ${conn.responseCode} $err")
        }

        conn.disconnect()
    }

    fun toggleAtivoEvento(token: String?, eventId: String, ativo: Boolean) {
        val url = URL("$EVENTS_URL/$eventId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        val json = JSONObject().apply { put("isDisabled", !ativo) }
        conn.outputStream.use { it.write(json.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            val err = BufferedReader(InputStreamReader(conn.errorStream)).use { it.readText() }
            throw IOException("Erro ao atualizar ativo: ${conn.responseCode} $err")
        }
        conn.disconnect()
    }
}
