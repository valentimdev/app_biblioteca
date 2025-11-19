package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.BookWithRentalStatus
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.adapters.LivroWithRentalAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val allBooks = mutableListOf<Book>()
    private val allBooksWithRental = mutableListOf<BookWithRentalStatus>()

    private lateinit var adapter: BookAdapter
    private lateinit var adapterWithRental: LivroWithRentalAdapter

    private var isAdmin = false
    private var selectedImageUri: Uri? = null

    companion object {
        const val REQUEST_CODE_IMAGE = 1001
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Adapter antigo para uso geral (BookAdapter)
        adapter = BookAdapter(allBooks, isAdmin) { action, book ->
            when (action) {
                "detail" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, isAdmin))
                    .addToBackStack(null)
                    .commit()
                "edit" -> if (isAdmin) showAddDialog(book)
                "remove" -> if (isAdmin) removeBook(book)
            }
        }
        rv.adapter = adapter

        // Pesquisa
        val edtSearch = view.findViewById<EditText>(R.id.edtSearch)
        edtSearch.doAfterTextChanged { text ->
            val filtered = allBooks.filter {
                it.title.contains(text.toString(), ignoreCase = true) ||
                        it.author.contains(text.toString(), ignoreCase = true)
            }
            adapter.updateData(filtered)
        }

        fetchBooks()
    }

    private fun fetchBooks() {
        val tokenStr = AuthUtils.getToken(requireContext())
        if (tokenStr.isNullOrEmpty()) return
        val token = "Bearer $tokenStr"

        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    adapter.updateData(allBooks)
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun showAddDialog(book: Book? = null) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_book, null)
        val edtTitulo = layout.findViewById<EditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<EditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<EditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<EditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<EditText>(R.id.edtTotalCopies)

        if (book != null) {
            edtTitulo.setText(book.title)
            edtAutor.setText(book.author)
            edtIsbn.setText(book.isbn ?: "")
            edtDescricao.setText(book.description ?: "")
            edtTotalCopies.setText(book.totalCopies.toString())
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (book == null) "Adicionar Livro" else "Editar Livro")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val tokenStr = AuthUtils.getToken(requireContext()) ?: return@setPositiveButton
                val token = "Bearer $tokenStr"

                val titlePart = edtTitulo.text.toString().toRequestBody("text/plain".toMediaType())
                val authorPart = edtAutor.text.toString().toRequestBody("text/plain".toMediaType())
                val isbnPart = edtIsbn.text.toString().toRequestBody("text/plain".toMediaType())
                val descriptionPart = edtDescricao.text.toString().toRequestBody("text/plain".toMediaType())
                val totalCopiesPart = (edtTotalCopies.text.toString().toIntOrNull() ?: 0)
                    .toString().toRequestBody("text/plain".toMediaType())

                val imagePart: MultipartBody.Part? = selectedImageUri?.let { uri ->
                    val file = File(requireContext().contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor?.let { fd ->
                        "/proc/self/fd/$fd"
                    } ?: uri.path ?: return@let null)
                    MultipartBody.Part.createFormData(
                        "image",
                        file.name,
                        file.asRequestBody("image/*".toMediaType())
                    )
                }

                if (book == null) {
                    RetrofitClient.bookApi.createBook(
                        titlePart, authorPart, isbnPart, descriptionPart, totalCopiesPart, imagePart, token
                    ).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) { fetchBooks() }
                        override fun onFailure(call: Call<Book>, t: Throwable) { t.printStackTrace() }
                    })
                } else {
                    val dto = EditBookDto(
                        title = edtTitulo.text.toString(),
                        author = edtAutor.text.toString(),
                        isbn = edtIsbn.text.toString(),
                        description = edtDescricao.text.toString(),
                        totalCopies = edtTotalCopies.text.toString().toIntOrNull()
                    )
                    RetrofitClient.bookApi.updateBook(book.id, dto, token).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) { fetchBooks() }
                        override fun onFailure(call: Call<Book>, t: Throwable) { t.printStackTrace() }
                    })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
        }
    }

    private fun removeBook(book: Book) {
        val token = "Bearer ${AuthUtils.getToken(requireContext())}"
        RetrofitClient.bookApi.deleteBook(book.id, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) { fetchBooks() }
            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) { t.printStackTrace() }
        })
    }
}
