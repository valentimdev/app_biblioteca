package com.example.bibliotecaunifor.api

import android.content.Context
import android.net.Uri
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
                eventos.add(
                    EventDto(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", null),
                        eventStartTime = obj.optString("eventStartTime", null),
                        eventEndTime = obj.optString("eventEndTime", null),
                        registrationStartTime = obj.optString("registrationStartTime", null),
                        registrationEndTime = obj.optString("registrationEndTime", null),
                        startTime = obj.optString("startTime", null),
                        endTime = obj.optString("endTime", null),
                        location = obj.optString("location", null),
                        imageUrl = obj.optString("imageUrl", null),
                        lecturers = obj.optString("lecturers", null),
                        seats = obj.optInt("seats", 0),
                        isDisabled = obj.optBoolean("isDisabled", false),
                        isFull = obj.optBoolean("isFull", false),
                        adminId = obj.optString("adminId", null),
                        createdAt = obj.optString("createdAt", null),
                        updatedAt = obj.optString("updatedAt", null)
                    )
                )
            }
        }
        conn.disconnect()
        return eventos
    }

    fun createEvent(context: Context, token: String?, evento: AdminEvento, imageUri: Uri?): AdminEvento {
        val boundary = "*****${System.currentTimeMillis()}*****"
        val url = URL(EVENTS_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        conn.doOutput = true

        val output = DataOutputStream(conn.outputStream)

        fun addFormField(name: String, value: String) {
            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
            output.writeBytes("$value\r\n")
        }

        addFormField("title", evento.title)
        evento.description?.let { addFormField("description", it) }
        evento.location?.let { addFormField("location", it) }

        evento.eventStartTime?.let { addFormField("eventStartTime", it) }
        evento.eventEndTime?.let { addFormField("eventEndTime", it) }
        evento.startTime?.let { addFormField("startTime", it) }
        evento.endTime?.let { addFormField("endTime", it) }
        evento.registrationStartTime?.let { addFormField("registrationStartTime", it) }
        evento.registrationEndTime?.let { addFormField("registrationEndTime", it) }

        addFormField("seats", evento.seats.toString())
        addFormField("isDisabled", evento.isDisabled.toString())

        evento.lecturers?.let { addFormField("lecturers", it) }

        imageUri?.let { uri ->
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "evento.jpg"
            val fileBytes = inputStream?.readBytes() ?: ByteArray(0)
            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n")
            output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            output.write(fileBytes)
            output.writeBytes("\r\n")
        }

        output.writeBytes("--$boundary--\r\n")
        output.flush()
        output.close()

        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream.bufferedReader().use { it.readText() }
            throw IOException("Erro ao criar evento: ${conn.responseCode} $err")
        }

        val resp = conn.inputStream.bufferedReader().use { it.readText() }
        val obj = JSONObject(resp)

        return AdminEvento(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.optString("description", null),
            eventStartTime = obj.optString("eventStartTime", null),
            eventEndTime = obj.optString("eventEndTime", null),
            registrationStartTime = obj.optString("registrationStartTime", null),
            registrationEndTime = obj.optString("registrationEndTime", null),
            startTime = obj.optString("startTime", null),
            endTime = obj.optString("endTime", null),
            location = obj.optString("location", null),
            imageUrl = obj.optString("imageUrl", null),
            lecturers = obj.optString("lecturers", null),
            seats = obj.getInt("seats"),
            isDisabled = obj.getBoolean("isDisabled"),
            isFull = obj.getBoolean("isFull"),
            adminId = obj.optString("adminId", null),
            createdAt = obj.optString("createdAt", null),
            updatedAt = obj.optString("updatedAt", null)
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
