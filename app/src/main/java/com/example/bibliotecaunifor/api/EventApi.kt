    package com.example.bibliotecaunifor.api

    import com.example.bibliotecaunifor.Evento
    import org.json.JSONArray
    import org.json.JSONObject
    import java.io.BufferedReader
    import java.io.InputStreamReader
    import java.net.HttpURLConnection
    import java.net.URL

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

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val evento = Evento(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", null),
                            startTime = obj.getString("eventStartTime"),
                            endTime = obj.getString("eventEndTime"),
                            location = obj.optString("location", null),
                            imageUrl = obj.optString("imageUrl", null),
                            lecturers = obj.optString("lecturers", null),
                            seats = obj.optInt("seats", 0),
                            isDisabled = obj.optBoolean("isDisabled", false),
                            createdAt = obj.optString("createdAt", ""),
                            updatedAt = obj.optString("updatedAt", "")
                        )

                        eventos.add(evento)
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

        fun updateEvent(eventId: String, token: String?, json: JSONObject) {
            val url = URL("${ApiConfig.BASE_URL}events/$eventId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PATCH"
            connection.setRequestProperty("Content-Type", "application/json")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            connection.doOutput = true

            connection.outputStream.use { os ->
                os.write(json.toString().toByteArray())
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                val reader = BufferedReader(InputStreamReader(connection.errorStream))
                val response = reader.readText()
                reader.close()
                throw Exception("Erro ao atualizar evento: $response")
            }

            connection.disconnect()
        }
    }
