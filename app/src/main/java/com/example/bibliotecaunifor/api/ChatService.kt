package com.example.bibliotecaunifor.api

import com.example.bibliotecaunifor.api.ApiConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ChatService {

    private val CHAT_URL = "${ApiConfig.BASE_URL}chat"

    /**
     * Envia uma mensagem para o Biblio Bot e retorna a resposta como String
     */
    fun sendMessage(token: String?, userPrompt: String): String {
        if (token.isNullOrEmpty()) {
            return "Erro: você precisa estar logado para usar o Biblio Bot."
        }

        val url = URL(CHAT_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        // Corpo da requisição (exatamente como o backend espera)
        val jsonBody = JSONObject().apply {
            put("prompt", userPrompt)
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonBody.toString())
            writer.flush()
        }

        return try {
            if (conn.responseCode in 200..299) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.optString("response", "Desculpe, não entendi a resposta do servidor.")
            } else {
                val error = conn.errorStream?.let {
                    BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
                } ?: "Erro ${conn.responseCode}"
                "Biblio Bot: $error"
            }
        } catch (e: Exception) {
            "Erro de conexão. Tente novamente."
        } finally {
            conn.disconnect()
        }
    }
}