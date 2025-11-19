package com.example.bibliotecaunifor.services

import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.api.ApiConfig
import com.example.bibliotecaunifor.Rental
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object BookService {

    private val BOOKS_URL = "${ApiConfig.BASE_URL}books"

    // ====== USUÁRIO ======

    fun getAllBooks(token: String?): List<Book> {
        val books = mutableListOf<Book>()
        val url = URL(BOOKS_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                books.add(Book(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    author = obj.getString("author"),
                    isbn = obj.getString("isbn"),
                    description = obj.optString("description"),
                    totalCopies = obj.getInt("totalCopies"),
                    availableCopies = obj.getInt("availableCopies"),
                    createdAt = obj.getString("createdAt"),
                    updatedAt = obj.getString("updatedAt"),
                    imageUrl = obj.getString("imageUrl")
                ))
            }
        } else {
            throw IOException("Erro ao carregar livros: ${conn.responseCode}")
        }
        conn.disconnect()
        return books
    }

    fun rentBook(token: String, bookId: String, dueDate: String): Boolean {
        // Primeiro verifica se o livro existe
        val getUrl = URL("$BOOKS_URL/$bookId")
        val getConn = getUrl.openConnection() as HttpURLConnection
        getConn.requestMethod = "GET"
        getConn.setRequestProperty("Authorization", "Bearer $token")

        if (getConn.responseCode !in 200..299) {
            getConn.disconnect()
            throw IOException("Livro não encontrado")
        }
        getConn.disconnect()

        // Agora aluga
        val url = URL("$BOOKS_URL/$bookId/rent")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        val json = JSONObject().apply { put("dueDate", dueDate) }
        conn.outputStream.use { it.write(json.toString().toByteArray()) }

        val success = conn.responseCode in 200..299
        conn.disconnect()
        return success
    }

    fun returnBook(token: String, bookId: String): Boolean {
        val url = URL("$BOOKS_URL/$bookId/return")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")

        val success = conn.responseCode in 200..299
        if (!success) {
            val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Erro desconhecido"
            throw IOException("Falha ao devolver: $error")
        }
        conn.disconnect()
        return success
    }

    fun getMyRentals(token: String): List<Rental> {
        val rentals = mutableListOf<Rental>()
        val url = URL("$BOOKS_URL/my-rentals")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val bookObj = obj.getJSONObject("book")
                rentals.add(Rental(
                    id = obj.getString("id"),
                    userId = obj.getString("userId"),
                    bookId = obj.getString("bookId"),
                    rentalDate = obj.getString("rentalDate"),
                    dueDate = obj.getString("dueDate"),
                    returnDate = obj.optString("returnDate", null),
                    book = Book(
                        id = bookObj.getString("id"),
                        title = bookObj.getString("title"),
                        author = bookObj.getString("author"),
                        isbn = bookObj.getString("isbn"),
                        description = bookObj.optString("description"),
                        totalCopies = bookObj.getInt("totalCopies"),
                        availableCopies = bookObj.getInt("availableCopies"),
                        createdAt = bookObj.getString("createdAt"),
                        updatedAt = bookObj.getString("updatedAt"),
                        imageUrl = bookObj.getString("imageUrl")
                    )
                ))
            }
        } else {
            throw IOException("Erro ao carregar empréstimos: ${conn.responseCode}")
        }
        conn.disconnect()
        return rentals
    }

    // ====== ADMIN ======

    fun createBook(token: String, book: Book): Book {
        val url = URL(BOOKS_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        val json = JSONObject().apply {
            put("title", book.title)
            put("author", book.author)
            put("isbn", book.isbn)
            put("description", book.description)
            put("totalCopies", book.totalCopies)
        }
        conn.outputStream.use { it.write(json.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Erro"
            throw IOException("Erro ao criar livro: $err")
        }

        val resp = conn.inputStream.bufferedReader().use { it.readText() }
        val respJson = JSONObject(resp)
        conn.disconnect()

        return Book(
            id = respJson.getString("id"),
            title = respJson.getString("title"),
            author = respJson.getString("author"),
            isbn = respJson.getString("isbn"),
            description = respJson.optString("description"),
            totalCopies = respJson.getInt("totalCopies"),
            availableCopies = respJson.getInt("availableCopies"),
            createdAt = respJson.getString("createdAt"),
            updatedAt = respJson.getString("updatedAt"),
            imageUrl = respJson.getString("imageUrl")
        )
    }

    fun updateBook(token: String, bookId: String, updates: Map<String, Any>) {
        val url = URL("$BOOKS_URL/$bookId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true

        val json = JSONObject(updates)
        conn.outputStream.use { it.write(json.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Erro"
            throw IOException("Erro ao atualizar livro: $err")
        }
        conn.disconnect()
    }

    fun deleteBook(token: String, bookId: String) {
        val url = URL("$BOOKS_URL/$bookId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Erro"
            throw IOException("Erro ao deletar livro: $err")
        }
        conn.disconnect()
    }
}