package com.example.bibliotecaunifor.network

import com.example.bibliotecaunifor.api.ApiConfig
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object NetworkHelper {

    private fun postJson(endpoint: String, jsonBody: JSONObject): JSONObject {
        val url = URL(ApiConfig.BASE_URL + endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connectTimeout = 8000
            readTimeout = 8000
        }

        conn.outputStream.use { os ->
            val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val responseCode = conn.responseCode
        val response = if (responseCode in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        conn.disconnect()
        return JSONObject(response)
    }

    fun login(matricula: String, password: String): Pair<String?, String?> {
        val jsonBody = JSONObject()
            .put("matricula", matricula)
            .put("password", password)

        return try {
            val response = postJson("auth/signin", jsonBody)
            val token = response.optString("access_token", null)
            val role = response.optString("role", null)
            Pair(token, role)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    fun signup(nome: String, email: String, matricula: String, password: String): Boolean {
        val jsonBody = JSONObject()
            .put("nome", nome)
            .put("email", email)
            .put("matricula", matricula)
            .put("password", password)

        return try {
            val response = postJson("auth/signup", jsonBody)
            response.has("access_token")
        } catch (e: Exception) {
            false
        }
    }
}
