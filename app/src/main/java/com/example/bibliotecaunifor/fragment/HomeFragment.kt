package com.example.bibliotecaunifor

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val loans = listOf(
        "MERIDIANO DE SANGUE 11/11/2025",
        "Authenticgames: vivendo uma vida autêntica 22/11/2025",
        "Mourinho (the special one) 15/11/2026"
    )

    private val recs = listOf(
        Book("10","Onde os Fracos Não Têm Vez","Cormac McCarthy"),
        Book("11","A Estrada","Cormac McCarthy"),
        Book("12","Sobre o Tempo","Autor X"),
        Book("13","Falando de Brinquedos e Luzes","Autor Y"),
        Book("14","Eu Sou Malala","Malala Yousafzai"),
        Book("15","O Caçador de Pipas","Khaled Hosseini")
    )

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("INICIO", showBack = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // monta a lista “na mão” só para simular como no mock
        val container = view.findViewById<LinearLayout>(R.id.loansList)
        loans.forEach { text ->
            val tv = TextView(requireContext()).apply {
                setPadding(8)
                this.text = "• $text"
                textSize = 14f
            }
            container.addView(tv)
        }

        // engrenagem -> popup “Renovar?”
        view.findViewById<View>(R.id.btnLoanOptions).setOnClickListener {
            showRenewDialog()
        }

        // recomendações (carrossel horizontal)
        val rv = view.findViewById<RecyclerView>(R.id.rvRecommendations)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = RecommendationAdapter(recs) { book ->
            // vai para o detalhe
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment.new(book))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showRenewDialog() {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_renew)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        d.findViewById<View>(R.id.btnYes).setOnClickListener { d.dismiss() /* TODO renovar */ }
        d.findViewById<View>(R.id.btnNo).setOnClickListener { d.dismiss() }
        d.show()
    }
}
