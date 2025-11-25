package com.example.bibliotecaunifor.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.fragment.BookDetailFragment
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

class CatalogAdminFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var edtSearch: EditText
    private var currentFilter: FilterType = FilterType.TODOS
    private val allBooks = mutableListOf<Book>()
    private var selectedImageUri: Uri? = null

    enum class FilterType { TODOS, VISIVEIS, OCULTOS, EMPRESTIMO_DESATIVADO }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvBooks = view.findViewById(R.id.rvBooks)
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)
        btnAdd.visibility = View.VISIBLE
        btnAdd.setOnClickListener { showAddDialog() }
        btnFilter.setOnClickListener { showFilterDialog() }
        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilterAndSearch()
            true
        }
        adapter = BookAdapter(allBooks, true) { action, book ->
            when (action) {
                "detail" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.admin_container, BookDetailFragment.newInstance(book, true))
                    .addToBackStack(null)
                    .commit()
                "edit" -> showAddDialog(book)
                "remove" -> removeBook(book)
            }
        }
        rvBooks.adapter = adapter
        fetchBooks()
    }

    private fun fetchBooks() {
        val token = AuthUtils.getToken(requireContext())
        if (token.isNullOrEmpty()) return
        RetrofitClient.bookApi.getBooks("Bearer $token").enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    applyFilterAndSearch()
                }
            }
            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun showFilterDialog() {
        val options = arrayOf("Todos", "Visíveis", "Ocultos", "Empréstimo desativado")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar livros")
            .setItems(options) { _, which ->
                currentFilter = when (which) {
                    1 -> FilterType.VISIVEIS
                    2 -> FilterType.OCULTOS
                    3 -> FilterType.EMPRESTIMO_DESATIVADO
                    else -> FilterType.TODOS
                }
                applyFilterAndSearch()
            }.show()
    }

    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        var list = allBooks.asSequence()
        if (query.isNotBlank()) list = list.filter { it.title.contains(query, true) || it.author.contains(query, true) }
        adapter.updateData(list.toList())
    }

    private fun showAddDialog(book: Book? = null) {
        val layout = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val edtTitulo = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtTotalCopies)
        val edtAvailableCopies = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtAvailableCopies)
        val imgCover = layout.findViewById<ImageView>(R.id.imgCover)
        val btnSelectImage = layout.findViewById<Button>(R.id.btnSelectImage)
        if (book != null) {
            edtTitulo.setText(book.title)
            edtAutor.setText(book.author)
            edtIsbn.setText(book.isbn ?: "")
            edtDescricao.setText(book.description ?: "")
            edtTotalCopies.setText(book.totalCopies.toString())
            edtAvailableCopies.setText(book.availableCopies.toString())
            if (!book.imageUrl.isNullOrEmpty()) Glide.with(this).load(book.imageUrl).into(imgCover)
        }
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (book == null) "Adicionar Livro" else "Editar Livro")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val tokenStr = AuthUtils.getToken(requireContext()) ?: return@setPositiveButton
                val token = "Bearer $tokenStr"
                if (book == null) createBook(edtTitulo, edtAutor, edtIsbn, edtDescricao, edtTotalCopies, edtAvailableCopies, token)
                else updateBook(book.id, edtTitulo, edtAutor, edtIsbn, edtDescricao, edtTotalCopies, edtAvailableCopies, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createBook(
        edtTitulo: com.google.android.material.textfield.TextInputEditText,
        edtAutor: com.google.android.material.textfield.TextInputEditText,
        edtIsbn: com.google.android.material.textfield.TextInputEditText,
        edtDescricao: com.google.android.material.textfield.TextInputEditText,
        edtTotalCopies: com.google.android.material.textfield.TextInputEditText,
        edtAvailableCopies: com.google.android.material.textfield.TextInputEditText,
        token: String
    ) {
        val titlePart = edtTitulo.text.toString().toRequestBody("text/plain".toMediaType())
        val authorPart = edtAutor.text.toString().toRequestBody("text/plain".toMediaType())
        val isbnPart = edtIsbn.text.toString().toRequestBody("text/plain".toMediaType())
        val descriptionPart = edtDescricao.text.toString().toRequestBody("text/plain".toMediaType())
        val totalCopiesPart = (edtTotalCopies.text.toString().toIntOrNull() ?: 0).toString().toRequestBody("text/plain".toMediaType())
        val availableCopiesPart = (edtAvailableCopies.text.toString().toIntOrNull() ?: 0).toString().toRequestBody("text/plain".toMediaType())

        val imagePart = selectedImageUri?.let { uri ->
            val file = File(uri.path!!)
            MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaType()))
        }

        RetrofitClient.bookApi.createBook(
            titlePart,
            authorPart,
            isbnPart,
            descriptionPart,
            totalCopiesPart,
            availableCopiesPart,
            imagePart,
            token
        )
    }

    private fun updateBook(
        bookId: String,
        edtTitulo: com.google.android.material.textfield.TextInputEditText,
        edtAutor: com.google.android.material.textfield.TextInputEditText,
        edtIsbn: com.google.android.material.textfield.TextInputEditText,
        edtDescricao: com.google.android.material.textfield.TextInputEditText,
        edtTotalCopies: com.google.android.material.textfield.TextInputEditText,
        edtAvailableCopies: com.google.android.material.textfield.TextInputEditText,
        token: String
    ) {
        val dto = EditBookDto(
            title = edtTitulo.text.toString(),
            author = edtAutor.text.toString(),
            isbn = edtIsbn.text.toString(),
            description = edtDescricao.text.toString(),
            totalCopies = edtTotalCopies.text.toString().toIntOrNull(),
            availableCopies = edtAvailableCopies.text.toString().toIntOrNull()
        )
        RetrofitClient.bookApi.updateBook(bookId, dto, token).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) { if (response.isSuccessful) fetchBooks() }
            override fun onFailure(call: Call<Book>, t: Throwable) {}
        })
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
            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {}
        })
    }

    companion object { const val REQUEST_CODE_IMAGE = 1001 }
}
