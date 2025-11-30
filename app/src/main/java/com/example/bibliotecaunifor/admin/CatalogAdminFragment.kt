package com.example.bibliotecaunifor.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.fragment.BookDetailFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CatalogAdminFragment : Fragment() {

    private lateinit var rvBooks: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var edtSearch: EditText
    private val allBooks = mutableListOf<Book>()

    // Variáveis para armazenar a imagem selecionada
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    // Referências da view do diálogo para poder atualizar via imagePicker
    private var dialogImgCover: ImageView? = null
    private var dialogEdtImageUrl: EditText? = null

    // Launcher para seleção de imagem
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                selectedImageFile = uriToFile(uri)

                // Atualizar a pré-visualização no diálogo
                dialogImgCover?.let { img ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_book)
                        .error(R.drawable.placeholder_book)
                        .into(img)
                }

                // Limpar o campo de URL quando uma imagem é selecionada
                dialogEdtImageUrl?.setText("")
            }
        }
    }

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
        // Resetar seleção de imagem
        selectedImageUri = null
        selectedImageFile = null

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

        // Guardar refs globais pro imagePicker usar
        dialogImgCover = imgCover
        dialogEdtImageUrl = edtImageUrl

        // Placeholder inicial
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

        // Botão para selecionar imagem da galeria
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Listener para pré-visualizar URL (quando não houver arquivo selecionado)
        edtImageUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && selectedImageFile == null) {
                val url = edtImageUrl.text.toString().trim()
                if (url.isNotBlank()) {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.placeholder_book)
                        .error(R.drawable.placeholder_book)
                        .into(imgCover)
                } else {
                    imgCover.setImageResource(R.drawable.placeholder_book)
                }
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

                // limpar refs do diálogo
                dialogImgCover = null
                dialogEdtImageUrl = null
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Limpar seleção ao cancelar
                selectedImageUri = null
                selectedImageFile = null
                dialogImgCover = null
                dialogEdtImageUrl = null
            }
            .show()
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

        val mediaType = "text/plain".toMediaTypeOrNull()

        // Preparar campos como RequestBody
        val titlePart: RequestBody = title.toRequestBody(mediaType)
        val authorPart: RequestBody = author.toRequestBody(mediaType)
        val isbnPart: RequestBody = isbn.toRequestBody(mediaType)
        val descPart: RequestBody = description.toRequestBody(mediaType)
        val totalPart: RequestBody = totalCopies.toRequestBody(mediaType)
        val availPart: RequestBody = availableCopies.toRequestBody(mediaType)

        // Preparar arquivo de imagem (se houver) ou URL
        val imagePart: MultipartBody.Part? = selectedImageFile?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        // Se não houver arquivo mas houver URL, enviar URL como campo normal
        val imageUrlPart: RequestBody? = if (imagePart == null && imageUrl.isNotBlank()) {
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
            imagePart,   // Arquivo como MultipartBody.Part
            imageUrlPart // URL (apenas se não houver arquivo)
        ).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        requireContext(),
                        "Livro cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpar seleção
                    selectedImageUri = null
                    selectedImageFile = null

                    fetchBooks()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Erro ao cadastrar livro (${response.code()}): $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Falha de conexão ao cadastrar: ${t.message}",
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
        val mediaType = "text/plain".toMediaTypeOrNull()

        // Preparar campos como RequestBody (apenas se não estiverem em branco)
        val titlePart: RequestBody? = title.takeIf { it.isNotBlank() }?.toRequestBody(mediaType)
        val authorPart: RequestBody? = author.takeIf { it.isNotBlank() }?.toRequestBody(mediaType)
        val isbnPart: RequestBody? = isbn.takeIf { it.isNotBlank() }?.toRequestBody(mediaType)
        val descPart: RequestBody? = description.takeIf { it.isNotBlank() }?.toRequestBody(mediaType)

        val totalPart: RequestBody? =
            totalCopies.takeIf { it.isNotBlank() }?.toIntOrNull()
                ?.let { it.toString().toRequestBody(mediaType) }

        val availPart: RequestBody? =
            availableCopies.takeIf { it.isNotBlank() }?.toIntOrNull()
                ?.let { it.toString().toRequestBody(mediaType) }

        // Preparar arquivo de imagem (se houver)
        val imagePart: MultipartBody.Part? = selectedImageFile?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        // Se não houver arquivo mas houver URL, enviar URL
        val imageUrlPart: RequestBody? = if (imagePart == null && imageUrl.isNotBlank()) {
            imageUrl.toRequestBody(mediaType)
        } else {
            null
        }

        RetrofitClient.bookApi.updateBook(
            bookId,
            titlePart,
            authorPart,
            isbnPart,
            descPart,
            totalPart,
            availPart,
            imagePart,   // Arquivo como MultipartBody.Part
            imageUrlPart // URL (apenas se não houver arquivo)
        ).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Livro atualizado!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpar seleção
                    selectedImageUri = null
                    selectedImageFile = null

                    fetchBooks()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Erro ao atualizar livro (${response.code()}): $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Falha de conexão ao atualizar: ${t.message}",
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
        // Companion vazio
    }
}
