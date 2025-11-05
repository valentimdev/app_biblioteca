package com.example.bibliotecaunifor.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.R

class AdminHomeFragment : Fragment(R.layout.fragment_admin_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            // barra proporcional
            val params = barra.layoutParams as LinearLayout.LayoutParams
            params.weight = qtd.toFloat() / max
            barra.layoutParams = params

            container.addView(item)
        }
    }
}
