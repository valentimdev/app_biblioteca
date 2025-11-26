package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.*
import com.example.bibliotecaunifor.adapters.EventoCarrosselAdapter
import com.example.bibliotecaunifor.adapters.LivroAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.UserResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileFragment : Fragment(R.layout.activity_perfil_usuario) {

    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null
    private var dialog: Dialog? = null

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
        // Garante atualização ao voltar do background
        carregarDadosDoUsuario()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botão editar perfil
        view.findViewById<ImageButton>(R.id.buttonEditarPerfil).setOnClickListener {
            mostrarPopupEditarPerfil(view)
        }

        // Botão configurações
        view.findViewById<ImageButton>(R.id.buttonConfiguracoes).setOnClickListener {
            startActivity(Intent(requireContext(), ConfiguracoesActivity::class.java))
        }

        // Botão notificações
        view.findViewById<ImageButton>(R.id.buttonNotificacoes).setOnClickListener {
            startActivity(Intent(requireContext(), NotificacoesActivity::class.java))
        }

        // Layout horizontal para livros
        view.findViewById<RecyclerView>(R.id.recyclerLivros).apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // Layout horizontal para eventos
        view.findViewById<RecyclerView>(R.id.recyclerEventos).apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    // FUNÇÃO PÚBLICA → chamada pelo MainActivity ao trocar de aba
    fun carregarDadosDoUsuario() {
        RetrofitClient.userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(requireContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
                    return
                }

                val user = response.body()!!

                view?.let { v ->
                    // Dados do usuário
                    v.findViewById<TextView>(R.id.textViewUsername).text = user.name
                    v.findViewById<TextView>(R.id.textViewEmail).text = user.email
                    v.findViewById<TextView>(R.id.textViewMatricula).text = user.matricula

                    // Foto de perfil
                    Glide.with(requireContext())
                        .load(user.imageUrl)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(v.findViewById(R.id.imageViewProfile))

                    // ==============================
                    // LIVROS EMPRESTADOS NO PERFIL
                    // ==============================
                    val recyclerLivros = v.findViewById<RecyclerView>(R.id.recyclerLivros)

                    // Se quiser histórico, troque para: filter { it.returnDate != null }
                    val livrosEmprestados = user.rentals
                        .filter { it.returnDate == null }   // apenas empréstimos ativos
                        .mapNotNull { it.book }             // pega apenas os Book não nulos

                    recyclerLivros.adapter = LivroAdapter(livrosEmprestados) { livro ->
                        mostrarDialogLivro(livro)
                    }

                    // ==============================
                    // EVENTOS INSCRITOS
                    // ==============================
                    val recyclerEventos = v.findViewById<RecyclerView>(R.id.recyclerEventos)
                    recyclerEventos.adapter = EventoCarrosselAdapter(
                        user.events.map { dto ->
                            Evento(
                                id = dto.id,
                                title = dto.title,
                                description = dto.description,
                                registrationStartTime = dto.registrationStartTime,
                                registrationEndTime = dto.registrationEndTime,
                                startTime = dto.eventStartTime,
                                endTime = dto.eventEndTime ?: "",
                                location = dto.location,
                                imageUrl = dto.imageUrl,
                                lecturers = dto.lecturers ?: "Sem palestrante",
                                seats = dto.seats,
                                isDisabled = dto.isDisabled,
                                createdAt = dto.createdAt ?: "",
                                updatedAt = dto.updatedAt ?: ""
                            )
                        }
                    ) { evento ->
                        mostrarDialogEvento(evento)
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarPopupEditarPerfil(view: View) {
        dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_editar_perfil)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setCancelable(true)
        }

        val editUsername = dialog!!.findViewById<EditText>(R.id.editUsername)
        val editEmail = dialog!!.findViewById<EditText>(R.id.editEmail)
        val imageViewProfileEdit = dialog!!.findViewById<ImageView>(R.id.imageViewProfileEdit)
        val buttonSalvar = dialog!!.findViewById<Button>(R.id.buttonSalvar)

        editUsername.setText(view.findViewById<TextView>(R.id.textViewUsername).text)
        editEmail.setText(view.findViewById<TextView>(R.id.textViewEmail).text)

        imageViewProfileEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSalvar.setOnClickListener {
            buttonSalvar.isEnabled = false
            buttonSalvar.text = "Salvando..."

            val namePart = editUsername.text.toString()
                .toRequestBody("text/plain".toMediaType())
            val emailPart = editEmail.text.toString()
                .toRequestBody("text/plain".toMediaType())

            var imagePart: MultipartBody.Part? = null
            selectedImageUri?.let { uri ->
                val resolver = requireContext().contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val inputStream = resolver.openInputStream(uri)!!
                val bytes = inputStream.readBytes()
                inputStream.close()

                val tempFile = File(
                    requireContext().cacheDir,
                    "upload_temp_${System.currentTimeMillis()}.jpg"
                )
                tempFile.writeBytes(bytes)

                val requestBody = tempFile.asRequestBody(mimeType.toMediaType())
                imagePart = MultipartBody.Part.createFormData(
                    "image",
                    tempFile.name,
                    requestBody
                )
            }

            RetrofitClient.userApi.editUserMultipart(namePart, emailPart, imagePart)
                .enqueue(object : Callback<UserResponse> {
                    override fun onResponse(
                        call: Call<UserResponse>,
                        response: Response<UserResponse>
                    ) {
                        buttonSalvar.isEnabled = true
                        buttonSalvar.text = "Salvar"

                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(
                                requireContext(),
                                "Perfil atualizado com sucesso!",
                                Toast.LENGTH_LONG
                            ).show()
                            carregarDadosDoUsuario() // Atualiza na hora
                            dialog?.dismiss()
                            selectedImageUri = null
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Erro ao salvar alterações",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                        buttonSalvar.isEnabled = true
                        buttonSalvar.text = "Salvar"
                        Toast.makeText(requireContext(), "Sem conexão", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        dialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data?.data != null
        ) {
            selectedImageUri = data.data!!
            selectedImageUri?.let { uri ->
                dialog?.findViewById<ImageView>(R.id.imageViewProfileEdit)?.setImageURI(uri)
            }
        }
    }

    private fun mostrarDialogLivro(livro: Book) {
        // Aqui você pode depois abrir tela de detalhes usando livro.id
        Toast.makeText(requireContext(), "Livro: ${livro.title}", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogEvento(evento: Evento) {
        // Implementar se necessário
        Toast.makeText(requireContext(), "Evento: ${evento.title}", Toast.LENGTH_SHORT).show()
    }
}
