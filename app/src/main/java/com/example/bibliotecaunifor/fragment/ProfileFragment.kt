package com.example.bibliotecaunifor.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.ConfiguracoesActivity
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.NotificacoesActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.EventoCarrosselAdapter
import com.example.bibliotecaunifor.adapters.LivroAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.EditUserRequest
import com.example.bibliotecaunifor.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.activity_perfil_usuario) {

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
        super.onViewCreated(view, savedInstanceState)

        buttonEditarPerfil.setOnClickListener { mostrarPopupEditarPerfil(view) }

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerEventos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        RetrofitClient.userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(requireContext(), "Erro ao carregar o usuário", Toast.LENGTH_SHORT).show()
                    return
                }

                val user = response.body()!!

                textViewUsername.text = user.name
                textViewEmail.text = user.email
                textViewMatricula.text = user.matricula

                Glide.with(requireContext())
                    .load(user.imageUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .into(imageViewProfile)

                // LISTA DE LIVROS
                val livros = user.rentals.map { it.book.title }
                recyclerLivros.adapter = LivroAdapter(livros) { livro ->
                    mostrarDialogLivro(livro)
                }

                val eventos = user.events.map {
                    Evento(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        startTime = it.startTime,
                        endTime = it.endTime ?: "",
                        location = it.location,
                        imageUrl = it.imageUrl,

                        lecturers = "",
                        seats = 0,
                        isDisabled = false,
                        adminId = "",
                        createdAt = "",
                        updatedAt = ""
                    )
                }

                recyclerEventos.adapter = EventoCarrosselAdapter(eventos) { evento ->
                    mostrarDialogEvento(evento)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Erro ao carregar perfil: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Botões superiores
        val buttonConfiguracoes = view.findViewById<ImageButton>(R.id.buttonConfiguracoes)
        val buttonNotificacoes = view.findViewById<ImageButton>(R.id.buttonNotificacoes)

        buttonConfiguracoes.setOnClickListener { startActivity(Intent(requireContext(), ConfiguracoesActivity::class.java)) }
        buttonNotificacoes.setOnClickListener { startActivity(Intent(requireContext(), NotificacoesActivity::class.java)) }
    }

    private fun mostrarPopupEditarPerfil(view: View) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_editar_perfil)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editUsername = dialog.findViewById<EditText>(R.id.editUsername)
        val editEmail = dialog.findViewById<EditText>(R.id.editEmail)
        val buttonSalvar = dialog.findViewById<Button>(R.id.buttonSalvar)

        val textViewUsername = view.findViewById<TextView>(R.id.textViewUsername)
        val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)

        editUsername.setText(textViewUsername.text)
        editEmail.setText(textViewEmail.text)

        buttonSalvar.setOnClickListener {
            val request = EditUserRequest(
                name = editUsername.text.toString().ifBlank { null },
                email = editEmail.text.toString().ifBlank { null }
            )

            RetrofitClient.userApi.editUser(request).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(requireContext(), "Erro ao salvar alterações", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val updated = response.body()!!
                    textViewUsername.text = updated.name
                    textViewEmail.text = updated.email

                    Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.show()
    }

    private fun mostrarDialogLivro(livro: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_info_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        dialog.findViewById<TextView>(R.id.textTituloLivro).text = livro
        dialog.findViewById<Button>(R.id.buttonFechar).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun mostrarDialogEvento(evento: Evento) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_info_evento)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        dialog.findViewById<TextView>(R.id.tvTituloEvento).text = evento.title
        dialog.findViewById<TextView>(R.id.tvDescricaoEvento).text = evento.description
        dialog.findViewById<TextView>(R.id.tvDataHoraEvento).text = evento.startTime

        dialog.findViewById<Button>(R.id.btnInscreverEvento).visibility = View.GONE
        dialog.findViewById<Button>(R.id.buttonFecharEvento).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
