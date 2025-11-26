package com.example.bibliotecaunifor.network

import com.example.bibliotecaunifor.admin.UserStatus
import com.example.bibliotecaunifor.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.withContext


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

    fun login(matricula: String, password: String): Triple<String?, String?, UserStatus?> {
        val jsonBody = JSONObject()
            .put("matricula", matricula)
            .put("password", password)

        return try {
            val response = postJson("auth/signin", jsonBody)

            // Extrai token e role (como antes)
            val token = response.optString("access_token", null)
            val role = response.optString("role", null)

            // Extrai o status do usuário
            val statusString = response.optString("status", null)
                ?: response.optJSONObject("user")?.optString("status", null)

            val status = when (statusString?.uppercase()) {
                "BANNED" -> UserStatus.BANNED
                "ACTIVE" -> UserStatus.ACTIVE
                else -> null
            }

            Triple(token, role, status)
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(null, null, null)
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
    suspend fun checkStatusFromError(matricula: String): UserStatus? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(ApiConfig.BASE_URL + "users/me")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000  // Mais rápido, só gambiarra
                    readTimeout = 5000
                    // NÃO manda token! Deixa falhar
                }

                val responseCode = conn.responseCode
                if (responseCode == 401) {  // Erro esperado
                    val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    val jsonError = try { JSONObject(errorResponse) } catch (e: Exception) { JSONObject() }

                    // Extrai user do corpo do erro (JwtGuard vaza isso)
                    val userJson = jsonError.optJSONObject("user")
                        ?: jsonError.optJSONObject("message")?.optJSONObject("user")

                    if (userJson != null) {
                        val statusStr = userJson.optString("status", "").uppercase()
                        return@withContext when (statusStr) {
                            "BANNED" -> UserStatus.BANNED
                            "ACTIVE" -> UserStatus.ACTIVE
                            else -> null
                        }
                    }
                }
                null  // Não é banido ou erro diferente
            } catch (e: Exception) {
                null
            }
        }
    }
}
