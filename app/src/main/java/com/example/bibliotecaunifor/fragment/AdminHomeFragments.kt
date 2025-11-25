package com.example.bibliotecaunifor.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.TopBook
import com.example.bibliotecaunifor.utils.AuthUtils

class AdminHomeFragment : Fragment(R.layout.fragment_admin_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserName()
        fetchDashboard()
    }

    private fun fetchUserName() {
        Thread {
            try {
                val response = RetrofitClient.userApi.getMe().execute()
                val name = if (response.isSuccessful)
                    response.body()?.name ?: "Fulano"
                else
                    "Fulano"

                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.tvUserGreeting)
                        ?.text = "Olá, $name 👋"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // -----------------------------------------
    // BUSCA TODAS AS ESTATÍSTICAS DO DASHBOARD
    // -----------------------------------------
    private fun fetchDashboard() {
        Thread {
            try {
                val token = AuthUtils.getToken(requireContext())
                val call = RetrofitClient.userApi.getDashboardStats("Bearer $token")
                val response = call.execute()

                if (!response.isSuccessful || response.body() == null) return@Thread
                val stats = response.body()!!

                requireActivity().runOnUiThread {

                    // Totais
                    view?.findViewById<TextView>(R.id.tvTotalLivros)
                        ?.text = stats.totalBooks.toString()

                    view?.findViewById<TextView>(R.id.tvTotalAlunos)
                        ?.text = stats.totalUsers.toString()

                    view?.findViewById<TextView>(R.id.tvLivrosAlugados)
                        ?.text = stats.totalRentedBooks.toString()

                    view?.findViewById<TextView>(R.id.tvLivrosDisponiveis)
                        ?.text = stats.availableBooks.toString()

                    // Top livros
                    renderTopBooks(stats.topRentedBooks)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // -----------------------------------------
    // RENDERIZA TOP 5 LIVROS MAIS EMPRESTADOS
    // -----------------------------------------
    private fun renderTopBooks(topBooks: List<TopBook>) {
        val container = view?.findViewById<LinearLayout>(R.id.containerTopLivros) ?: return
        container.removeAllViews()

        if (topBooks.isEmpty()) return

        val max = topBooks.maxOf { it.totalRentals }
        val inflater = LayoutInflater.from(requireContext())

        topBooks.forEach { book ->
            val item = inflater.inflate(R.layout.item_top_livro, container, false)

            val tvNome = item.findViewById<TextView>(R.id.tvLivroNome)
            val barra = item.findViewById<View>(R.id.barraProgresso)
            val tvQtd = item.findViewById<TextView>(R.id.tvQtd)

            tvNome.text = book.title
            tvQtd.text = book.totalRentals.toString()

            val params = barra.layoutParams as LinearLayout.LayoutParams
            params.weight = book.totalRentals.toFloat() / max
            barra.layoutParams = params

            container.addView(item)
        }
    }
}
