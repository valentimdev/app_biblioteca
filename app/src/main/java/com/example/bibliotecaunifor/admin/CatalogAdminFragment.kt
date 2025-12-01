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
    private lateinit var adapter: AdminBooksAdapter
    private lateinit var edtSearch: EditText

    // Lista completa para filtro/pesquisa
    private val allBooks = mutableListOf<Book>()

    // Estados locais (visibilidade e empréstimo)
    private val emprestimoState = mutableMapOf<String, Boolean>()
    private val visibilidadeState = mutableMapOf<String, Boolean>()

    // Imagem selecionada
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    // Referência da imageView do diálogo
    private var dialogImgCover: ImageView? = null

    // Limite de 5 MB
    private val MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024

    // Launcher para seleção de imagem
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                selectedImageFile = uriToFile(uri)

                dialogImgCover?.let { img ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_book)
                        .error(R.drawable.placeholder_book)
                        .into(img)
                }
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

        adapter = AdminBooksAdapter(
            books = mutableListOf(),
            emprestimoState = emprestimoState,
            visibilidadeState = visibilidadeState,
            onEdit = { book ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.admin_container,
                        BookDetailFragment.newInstance(book, true)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            onRemove = { book -> removeBook(book) },
            onToggleEmprestimo = { book ->
                val atual = emprestimoState[book.id] ?: true
                val novo = !atual

                emprestimoState[book.id] = novo
                adapter.notifyBookChanged(book.id)

                RetrofitClient.bookApi.patchBookFlags(
                    book.id,
                    mapOf("loanEnabled" to novo)
                ).enqueue(object : Callback<Book> {
                    override fun onResponse(call: Call<Book>, response: Response<Book>) {
                        if (!response.isSuccessful || response.body() == null) {
                            emprestimoState[book.id] = atual
                            adapter.notifyBookChanged(book.id)
                            Toast.makeText(
                                requireContext(),
                                "Erro ao atualizar empréstimo (${response.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val updated = response.body()!!
                            emprestimoState[book.id] = updated.loanEnabled
                            adapter.notifyBookChanged(book.id)
                        }
                    }

                    override fun onFailure(call: Call<Book>, t: Throwable) {
                        emprestimoState[book.id] = atual
                        adapter.notifyBookChanged(book.id)
                        Toast.makeText(
                            requireContext(),
                            "Falha de conexão ao alterar empréstimo: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            },
            onToggleVisibilidade = { book ->
                val atualVisivel = visibilidadeState[book.id] ?: true
                val novoVisivel = !atualVisivel

                visibilidadeState[book.id] = novoVisivel
                adapter.notifyBookChanged(book.id)

                val novoIsHidden = !novoVisivel

                RetrofitClient.bookApi.patchBookFlags(
                    book.id,
                    mapOf("isHidden" to novoIsHidden)
                ).enqueue(object : Callback<Book> {
                    override fun onResponse(call: Call<Book>, response: Response<Book>) {
                        if (!response.isSuccessful || response.body() == null) {
                            visibilidadeState[book.id] = atualVisivel
                            adapter.notifyBookChanged(book.id)
                            Toast.makeText(
                                requireContext(),
                                "Erro ao atualizar visibilidade (${response.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val updated = response.body()!!
                            visibilidadeState[book.id] = !updated.isHidden
                            adapter.notifyBookChanged(book.id)
                        }
                    }

                    override fun onFailure(call: Call<Book>, t: Throwable) {
                        visibilidadeState[book.id] = atualVisivel
                        adapter.notifyBookChanged(book.id)
                        Toast.makeText(
                            requireContext(),
                            "Falha de conexão ao alterar visibilidade: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        )

        rvBooks.adapter = adapter
        fetchBooks()
    }

    private fun fetchBooks() {
        RetrofitClient.bookApi.getAdminBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    allBooks.clear()
                    val lista = response.body() ?: emptyList()
                    allBooks.addAll(lista)

                    emprestimoState.clear()
                    visibilidadeState.clear()
                    lista.forEach { book ->
                        emprestimoState[book.id] = book.loanEnabled
                        visibilidadeState[book.id] = !book.isHidden
                    }

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
                Toast.makeText(
                    requireContext(),
                    "Falha ao carregar livros",
                    Toast.LENGTH_SHORT
                ).show()
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

        adapter.replaceAll(filtered)
    }

    private fun showAddDialog(book: Book? = null) {
        selectedImageUri = null
        selectedImageFile = null

        val layout = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val edtTitulo = layout.findViewById<EditText>(R.id.edtTitulo)
        val edtAutor = layout.findViewById<EditText>(R.id.edtAutor)
        val edtIsbn = layout.findViewById<EditText>(R.id.edtIsbn)
        val edtDescricao = layout.findViewById<EditText>(R.id.edtDescricao)
        val edtTotalCopies = layout.findViewById<EditText>(R.id.edtTotalCopies)
        val edtAvailableCopies = layout.findViewById<EditText>(R.id.edtAvailableCopies)
        val imgCover = layout.findViewById<ImageView>(R.id.imgCover)
        val btnSelectImage = layout.findViewById<Button>(R.id.btnSelectImage)

        dialogImgCover = imgCover

        imgCover.setImageResource(R.drawable.placeholder_book)

        if (book != null) {
            edtTitulo.setText(book.title)
            edtAutor.setText(book.author)
            edtIsbn.setText(book.isbn ?: "")
            edtDescricao.setText(book.description ?: "")
            edtTotalCopies.setText(book.totalCopies.toString())
            edtAvailableCopies.setText(book.availableCopies.toString())

            if (!book.imageUrl.isNullOrEmpty()) {
                Glide.with(this).load(book.imageUrl).into(imgCover)
            }
        }

        btnSelectImage.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
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

                if (book == null) {
                    createBook(title, author, isbn, description, total, available)
                } else {
                    updateBook(
                        book.id,
                        title,
                        author,
                        isbn,
                        description,
                        total,
                        available
                    )
                }

                dialogImgCover = null
            }
            .setNegativeButton("Cancelar") { _, _ ->
                selectedImageUri = null
                selectedImageFile = null
                dialogImgCover = null
            }
            .show()
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(uri)
            val file = File(
                requireContext().cacheDir,
                "temp_image_${System.currentTimeMillis()}.jpg"
            )
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

    private fun getImageMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }

    private fun showImageTooLargeError() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Imagem muito grande")
            .setMessage("A imagem precisa ter no máximo 5 MB. Escolha uma imagem menor ou comprima antes de enviar.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun createBook(
        title: String,
        author: String,
        isbn: String,
        description: String,
        totalCopies: String,
        availableCopies: String
    ) {
        if (title.isBlank() || author.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Título e autor são obrigatórios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        selectedImageFile?.let { file ->
            if (file.length() > MAX_IMAGE_SIZE_BYTES) {
                showImageTooLargeError()
                return
            }
        }

        val mediaType = "text/plain".toMediaTypeOrNull()

        val titlePart: RequestBody = title.toRequestBody(mediaType)
        val authorPart: RequestBody = author.toRequestBody(mediaType)
        val isbnPart: RequestBody = isbn.toRequestBody(mediaType)
        val descPart: RequestBody = description.toRequestBody(mediaType)
        val totalPart: RequestBody = totalCopies.toRequestBody(mediaType)
        val availPart: RequestBody = availableCopies.toRequestBody(mediaType)

        val imagePart: MultipartBody.Part? = selectedImageFile?.let { file ->
            val mimeType = getImageMimeType(file)
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        // imageUrl agora sempre null (não usamos mais URL)
        val imageUrlPart: RequestBody? = null

        RetrofitClient.bookApi.createBook(
            titlePart,
            authorPart,
            isbnPart,
            descPart,
            totalPart,
            availPart,
            imagePart,
            imageUrlPart
        ).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        requireContext(),
                        "Livro cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedImageUri = null
                    selectedImageFile = null
                    fetchBooks()
                } else {
                    val errorBody = response.errorBody()?.string()

                    val isImageTooLarge =
                        response.code() == 413 ||
                                (errorBody?.contains("File too large", true) == true) ||
                                (errorBody?.contains("file size", true) == true)

                    if (isImageTooLarge) {
                        showImageTooLargeError()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao cadastrar livro (${response.code()}): $errorBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
        availableCopies: String
    ) {
        val mediaType = "text/plain".toMediaTypeOrNull()

        selectedImageFile?.let { file ->
            if (file.length() > MAX_IMAGE_SIZE_BYTES) {
                showImageTooLargeError()
                return
            }
        }

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

        val imagePart: MultipartBody.Part? = selectedImageFile?.let { file ->
            val mimeType = getImageMimeType(file)
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        val imageUrlPart: RequestBody? = null

        RetrofitClient.bookApi.updateBook(
            bookId,
            titlePart,
            authorPart,
            isbnPart,
            descPart,
            totalPart,
            availPart,
            imagePart,
            imageUrlPart
        ).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Livro atualizado!",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedImageUri = null
                    selectedImageFile = null
                    fetchBooks()
                } else {
                    val errorBody = response.errorBody()?.string()

                    val isImageTooLarge =
                        response.code() == 413 ||
                                (errorBody?.contains("File too large", true) == true) ||
                                (errorBody?.contains("file size", true) == true)

                    if (isImageTooLarge) {
                        showImageTooLargeError()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao atualizar livro (${response.code()}): $errorBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao remover livro (${response.code()}): $errorBody",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, Boolean>>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "Falha de conexão ao remover: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            .setNegativeButton("Não", null)
            .show()
    }

    companion object
}
