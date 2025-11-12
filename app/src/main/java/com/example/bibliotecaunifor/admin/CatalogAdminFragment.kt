package com.example.bibliotecaunifor.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.BookDetailFragment
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.CreateBookDto
import com.example.bibliotecaunifor.models.EditBookDto
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogAdminFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var edtSearch: EditText
    private var currentFilter: FilterType = FilterType.TODOS

    private val allBooks = mutableListOf<Book>()

    enum class FilterType { TODOS, VISIVEIS, OCULTOS, EMPRESTIMO_DESATIVADO }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_catalog, container, false)

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
        if (token.isNullOrEmpty()) {
            android.util.Log.e("CatalogAdminFragment", "Token nulo!")
            return
        }

        RetrofitClient.bookApi.getBooks("Bearer $token").enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                android.util.Log.d("CatalogAdminFragment", "Response code: ${response.code()}")
                android.util.Log.d("CatalogAdminFragment", "Body: ${response.body()}")
                if (response.isSuccessful) {
                    allBooks.clear()
                    allBooks.addAll(response.body() ?: emptyList())
                    applyFilterAndSearch()
                } else {
                    android.util.Log.e("CatalogAdminFragment", "Erro: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                android.util.Log.e("CatalogAdminFragment", "Falha ao buscar livros", t)
            }
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
            }
            .show()
    }

    private fun applyFilterAndSearch() {
        val query = edtSearch.text.toString().trim()
        var list = allBooks.asSequence()

        if (query.isNotBlank()) {
            list = list.filter { it.title.contains(query, true) || it.author.contains(query, true) }
        }

        adapter.updateData(list.toList())
    }

    private fun showAddDialog(book: Book? = null) {
        val layout = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val edtTitulo = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtTotalCopies)

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
                val tokenStr = AuthUtils.getToken(requireContext())
                if (tokenStr.isNullOrEmpty()) {
                    android.util.Log.e("CatalogAdminFragment", "Token nulo!")
                    return@setPositiveButton
                }
                val token = "Bearer $tokenStr"

                if (book == null) {
                    val dto = CreateBookDto(
                        title = edtTitulo.text.toString(),
                        author = edtAutor.text.toString(),
                        isbn = edtIsbn.text.toString(),
                        description = edtDescricao.text.toString(),
                        totalCopies = edtTotalCopies.text.toString().toIntOrNull() ?: 0
                    )
                    android.util.Log.d("CatalogAdminFragment", "Criando livro: $dto")

                    RetrofitClient.bookApi.createBook(dto, token).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) {
                            if (response.isSuccessful) {
                                android.util.Log.d("CatalogAdminFragment", "Livro criado: ${response.body()}")
                                fetchBooks()
                            } else {
                                android.util.Log.e("CatalogAdminFragment", "Erro ao criar: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<Book>, t: Throwable) {
                            android.util.Log.e("CatalogAdminFragment", "Falha na criação do livro", t)
                        }
                    })
                } else {
                    val dto = EditBookDto(
                        title = edtTitulo.text.toString(),
                        author = edtAutor.text.toString(),
                        isbn = edtIsbn.text.toString(),
                        description = edtDescricao.text.toString(),
                        totalCopies = edtTotalCopies.text.toString().toIntOrNull()
                    )
                    android.util.Log.d("CatalogAdminFragment", "Editando livro: $dto")

                    RetrofitClient.bookApi.updateBook(book.id, dto, token).enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) {
                            if (response.isSuccessful) {
                                android.util.Log.d("CatalogAdminFragment", "Livro editado: ${response.body()}")
                                fetchBooks()
                            } else {
                                android.util.Log.e("CatalogAdminFragment", "Erro ao editar: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<Book>, t: Throwable) {
                            android.util.Log.e("CatalogAdminFragment", "Falha na edição do livro", t)
                        }
                    })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removeBook(book: Book) {
        val token = "Bearer ${AuthUtils.getToken(requireContext())}"
        RetrofitClient.bookApi.deleteBook(book.id, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) { fetchBooks() }
            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) { t.printStackTrace() }
        })
    }
}
