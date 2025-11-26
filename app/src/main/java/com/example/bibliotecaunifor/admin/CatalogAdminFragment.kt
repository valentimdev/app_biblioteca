package com.example.bibliotecaunifor.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.fragment.BookDetailFragment
import com.example.bibliotecaunifor.models.EditBookDto
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogAdminFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var edtSearch: EditText
    private val allBooks = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBooks = view.findViewById(R.id.rvBooks)
        edtSearch = view.findViewById(R.id.edtSearch)
        val btnFilter = view.findViewById<View>(R.id.btnFilter)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
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
                "toggleEmprestimo" -> {
                    // se tiver endpoint depois, plugamos aqui
                }
                "toggleVisibilidade" -> {
                    // idem
                }
            }
        }
        rvBooks.adapter = adapter

        fetchBooks()
    }

    private fun fetchBooks() {
        RetrofitClient.bookApi.getBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    applyFilterAndSearch()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao buscar livros (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                Toast.makeText(requireContext(), "Falha ao carregar livros", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showFilterDialog() {
        val options = arrayOf("Todos", "Disponíveis", "Indisponíveis")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar livros")
            .setItems(options) { _, _ ->
                // Por enquanto só reaplica pesquisa
                applyFilterAndSearch()
            }
            .show()
    }

    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        val filtered = allBooks.filter { book ->
            query.isBlank() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    (book.isbn?.contains(query, ignoreCase = true) == true)
        }
        adapter.updateData(filtered)
    }

    private fun showAddDialog(book: Book? = null) {
        val layout = layoutInflater.inflate(R.layout.dialog_add_book, null)

        val edtTitulo = layout.findViewById<EditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<EditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<EditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<EditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<EditText>(R.id.edtTotalCopies)
        val edtAvailableCopies = layout.findViewById<EditText>(R.id.edtAvailableCopies)
        val edtImageUrl = layout.findViewById<EditText>(R.id.edtImageUrl)
        val imgCover = layout.findViewById<ImageView>(R.id.imgCover)
        val btnSelectImage = layout.findViewById<Button>(R.id.btnSelectImage)

        // placeholder inicial
        imgCover.setImageResource(R.drawable.placeholder_book)

        if (book != null) {
            edtTitulo.setText(book.title)
            edtAutor.setText(book.author)
            edtIsbn.setText(book.isbn ?: "")
            edtDescricao.setText(book.description ?: "")
            edtTotalCopies.setText(book.totalCopies.toString())
            edtAvailableCopies.setText(book.availableCopies.toString())
            edtImageUrl.setText(book.imageUrl ?: "")

            if (!book.imageUrl.isNullOrEmpty()) {
                Glide.with(this).load(book.imageUrl).into(imgCover)
            }
        }

        // agora esse botão serve para PRÉ-VISUALIZAR a URL digitada
        btnSelectImage.setOnClickListener {
            val url = edtImageUrl.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(requireContext(), "Informe a URL da imagem primeiro", Toast.LENGTH_SHORT).show()
            } else {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(imgCover)
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (book == null) "Adicionar Livro" else "Editar Livro")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val title = edtTitulo.text.toString()
                val author = edtAutor.text.toString()
                val isbn = edtIsbn.text.toString()
                val description = edtDescricao.text.toString()
                val total = edtTotalCopies.text.toString()
                val available = edtAvailableCopies.text.toString()
                val imageUrl = edtImageUrl.text.toString()

                if (book == null) {
                    createBook(title, author, isbn, description, total, available, imageUrl)
                } else {
                    updateBook(book.id, title, author, isbn, description, total, available, imageUrl)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createBook(
        title: String,
        author: String,
        isbn: String,
        description: String,
        totalCopies: String,
        availableCopies: String,
        imageUrl: String
    ) {
        if (title.isBlank() || author.isBlank()) {
            Toast.makeText(requireContext(), "Título e autor são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaType = "text/plain".toMediaType()

        val titlePart: RequestBody = title.toRequestBody(mediaType)
        val authorPart: RequestBody = author.toRequestBody(mediaType)
        val isbnPart: RequestBody = isbn.toRequestBody(mediaType)
        val descPart: RequestBody = description.toRequestBody(mediaType)
        val totalPart: RequestBody = totalCopies.toRequestBody(mediaType)
        val availPart: RequestBody = availableCopies.toRequestBody(mediaType)
        val imageUrlPart: RequestBody? = if (imageUrl.isNotBlank()) {
            imageUrl.toRequestBody(mediaType)
        } else {
            null
        }

        RetrofitClient.bookApi.createBook(
            titlePart,
            authorPart,
            isbnPart,
            descPart,
            totalPart,
            availPart,
            imageUrlPart
        ).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        requireContext(),
                        "Livro cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchBooks()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao cadastrar livro (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Falha de conexão ao cadastrar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateBook(
        bookId: String,
        title: String,
        author: String,
        isbn: String,
        description: String,
        totalCopies: String,
        availableCopies: String,
        imageUrl: String
    ) {
        val dto = EditBookDto(
            title = title,
            author = author,
            isbn = isbn,
            description = description,
            totalCopies = totalCopies.toIntOrNull(),
            availableCopies = availableCopies.toIntOrNull()
        )

        RetrofitClient.bookApi.updateBook(bookId, dto)
            .enqueue(object : Callback<Book> {
                override fun onResponse(call: Call<Book>, response: Response<Book>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Livro atualizado!",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchBooks()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao atualizar livro (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Book>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Falha de conexão ao atualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun removeBook(book: Book) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir livro")
            .setMessage("Tem certeza que deseja excluir \"${book.title}\"?")
            .setPositiveButton("Sim") { _, _ ->
                RetrofitClient.bookApi.deleteBook(book.id)
                    .enqueue(object : Callback<Map<String, Boolean>> {
                        override fun onResponse(
                            call: Call<Map<String, Boolean>>,
                            response: Response<Map<String, Boolean>>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Livro removido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                fetchBooks()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao remover livro (${response.code()})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, Boolean>>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "Falha de conexão ao remover",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            .setNegativeButton("Não", null)
            .show()
    }

    companion object {
        // não precisamos mais de REQUEST_CODE_IMAGE, mas deixei o companion vazio
    }
}
