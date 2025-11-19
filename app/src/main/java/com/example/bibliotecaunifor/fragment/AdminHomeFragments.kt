package com.example.bibliotecaunifor.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.services.BookService
import com.example.bibliotecaunifor.utils.AuthUtils
import com.example.bibliotecaunifor.api.RetrofitClient

class AdminHomeFragment : Fragment(R.layout.fragment_admin_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserName()
        fetchTotals()

        val container = view.findViewById<LinearLayout>(R.id.containerTopLivros)

        val topLivros = listOf(
            "A Revolução dos Bichos" to 230,
            "O Pequeno Príncipe" to 180,
            "Dom Casmurro" to 150,
            "A Menina que Roubava Livros" to 120,
            "1984" to 95
        )

        val max = topLivros.maxOf { it.second }

        val inflater = LayoutInflater.from(requireContext())

        topLivros.forEach { (titulo, qtd) ->
            val item = inflater.inflate(R.layout.item_top_livro, container, false)
            val tvNome = item.findViewById<TextView>(R.id.tvLivroNome)
            val barra = item.findViewById<View>(R.id.barraProgresso)
            val tvQtd = item.findViewById<TextView>(R.id.tvQtd)

            tvNome.text = titulo
            tvQtd.text = "$qtd"

            val params = barra.layoutParams as LinearLayout.LayoutParams
            params.weight = qtd.toFloat() / max
            barra.layoutParams = params

            container.addView(item)
        }
    }

    private fun fetchUserName() {
        Thread {
            try {
                val call = RetrofitClient.userApi.getMe()
                val response = call.execute()
                val name = if (response.isSuccessful) response.body()?.name ?: "Fulano" else "Fulano"

                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.tvUserGreeting)?.text = "Olá, $name 👋"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun fetchTotals() {
        Thread {
            try {
                val token = AuthUtils.getToken(requireContext())

                val livros = BookService.getAllBooks(token)
                val totalLivros = livros.size

                val call = RetrofitClient.userApi.getAllUsers("Bearer $token")
                val response = call.execute()
                val totalAlunos = if (response.isSuccessful) response.body()?.size ?: 0 else 0

                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.tvTotalLivros)?.text = totalLivros.toString()
                    view?.findViewById<TextView>(R.id.tvTotalAlunos)?.text = totalAlunos.toString()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
