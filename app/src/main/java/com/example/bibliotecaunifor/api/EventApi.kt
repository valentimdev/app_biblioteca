package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.Evento
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object EventApi {

    fun fetchEventos(): List<Evento> {
        val url = URL("${ApiConfig.BASE_URL}events")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000

        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArray = JSONArray(response)
                val eventos = mutableListOf<Evento>()
                val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val titulo = obj.getString("title")
                    val descricao = obj.optString("description", "")
                    val data = Calendar.getInstance().apply {
                        time = formato.parse(obj.getString("startTime")) ?: Date()
                    }
                    eventos.add(Evento(titulo, data, descricao))
                }

                eventos
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            connection.disconnect()
        }
    }
}
