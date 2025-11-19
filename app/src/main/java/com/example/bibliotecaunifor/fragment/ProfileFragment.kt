package com.example.bibliotecaunifor.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.*
import com.example.bibliotecaunifor.adapters.EventoCarrosselAdapter
import com.example.bibliotecaunifor.adapters.LivroAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.EditUserRequest
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val buttonEditarPerfil = view.findViewById<ImageButton>(R.id.buttonEditarPerfil)
        val recyclerLivros = view.findViewById<RecyclerView>(R.id.recyclerLivros)
        val recyclerEventos = view.findViewById<RecyclerView>(R.id.recyclerEventos)

        val textViewUsername = view.findViewById<TextView>(R.id.textViewUsername)
        val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)
        val textViewMatricula = view.findViewById<TextView>(R.id.textViewMatricula)
        val imageViewProfile = view.findViewById<ImageView>(R.id.imageViewProfile)

        val buttonConfiguracoes = view.findViewById<ImageButton>(R.id.buttonConfiguracoes)
        val buttonNotificacoes = view.findViewById<ImageButton>(R.id.buttonNotificacoes)

        buttonConfiguracoes.setOnClickListener {
            startActivity(Intent(requireContext(), ConfiguracoesActivity::class.java))
        }

        buttonNotificacoes.setOnClickListener {
            startActivity(Intent(requireContext(), NotificacoesActivity::class.java))
        }

        super.onViewCreated(view, savedInstanceState)

        buttonEditarPerfil.setOnClickListener { mostrarPopupEditarPerfil(view) }

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerEventos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        RetrofitClient.userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful || response.body() == null) return
                val user = response.body()!!
                textViewUsername.text = user.name
                textViewEmail.text = user.email
                textViewMatricula.text = user.matricula

                Glide.with(requireContext())
                    .load(user.imageUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .into(imageViewProfile)

                recyclerLivros.adapter = LivroAdapter(user.rentals.map { it.book.title }) { mostrarDialogLivro(it) }
                recyclerEventos.adapter = EventoCarrosselAdapter(user.events.map {
                    Evento(it.id, it.title, it.description, it.startTime, it.endTime ?: "", it.location, it.imageUrl, "",0,false,"","","")
                }) { mostrarDialogEvento(it) }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {}
        })
    }

    private fun mostrarPopupEditarPerfil(view: View) {
        dialog = Dialog(requireContext())
        dialog!!.setContentView(R.layout.dialog_editar_perfil)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val editUsername = dialog!!.findViewById<EditText>(R.id.editUsername)
        val editEmail = dialog!!.findViewById<EditText>(R.id.editEmail)
        val imageViewProfileEdit = dialog!!.findViewById<ImageView>(R.id.imageViewProfileEdit)
        val buttonSalvar = dialog!!.findViewById<Button>(R.id.buttonSalvar)

        val textViewUsername = view.findViewById<TextView>(R.id.textViewUsername)
        val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)

        editUsername.setText(textViewUsername.text)
        editEmail.setText(textViewEmail.text)

        imageViewProfileEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSalvar.setOnClickListener {
            val namePart = editUsername.text.toString().toRequestBody("text/plain".toMediaType())
            val emailPart = editEmail.text.toString().toRequestBody("text/plain".toMediaType())

            var imagePart: MultipartBody.Part? = null
            selectedImageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val tempFile = File(requireContext().cacheDir, "temp_image.jpg")
                tempFile.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }

                val requestBody = tempFile.asRequestBody("image/*".toMediaType())
                imagePart = MultipartBody.Part.createFormData(
                    "image",
                    tempFile.name,
                    requestBody
                )
            }
            RetrofitClient.userApi.editUserMultipart(namePart, emailPart, imagePart)
                .enqueue(object : Callback<UserResponse> {
                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                        if (!response.isSuccessful || response.body() == null) return
                        val updated = response.body()!!
                        textViewUsername.text = updated.name
                        textViewEmail.text = updated.email
                        if (updated.imageUrl != null) {
                            Glide.with(requireContext())
                                .load(updated.imageUrl)
                                .placeholder(R.drawable.placeholder_user)
                                .error(R.drawable.placeholder_user)
                                .into(view.findViewById(R.id.imageViewProfile))
                        }
                        Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                        dialog?.dismiss()
                    }

                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {}
                })
        }

        dialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            dialog?.findViewById<ImageView>(R.id.imageViewProfileEdit)?.setImageURI(selectedImageUri)
        }
    }

    private fun mostrarDialogLivro(livro: String) { /* ... */ }
    private fun mostrarDialogEvento(evento: Evento) { /* ... */ }
}
