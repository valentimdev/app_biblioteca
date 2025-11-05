package com.example.bibliotecaunifor

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    // cada empréstimo com título e data
    data class Loan(
        val title: String,
        val due: String
    )

    // mock de empréstimos
    private val loans = listOf(
        Loan("MERIDIANO DE SANGUE", "11/11/2025"),
        Loan("Authenticgames: vivendo uma vida autêntica", "22/11/2025"),
        Loan("Mourinho (the special one)", "15/11/2026")
    )

    // mock de recomendações (ajuste para o seu Book real)
    private val recs = listOf(
        Book("10", "Onde os Fracos Não Têm Vez", "Cormac McCarthy"),
        Book("11", "A Estrada", "Cormac McCarthy"),
        Book("12", "Sobre o Tempo", "Autor X"),
        Book("13", "Falando de Brinquedos e Luzes", "Autor Y"),
        Book("14", "Eu Sou Malala", "Malala Yousafzai"),
        Book("15", "O Caçador de Pipas", "Khaled Hosseini")
    )

    override fun onResume() {
        super.onResume()
        // atualiza o título/topbar quando esta tela volta
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) monta a lista de empréstimos dentro do card vermelho
        val loansContainer = view.findViewById<LinearLayout>(R.id.loansList)
        val inflater = LayoutInflater.from(requireContext())

        loans.forEach { loan ->
            // inflamos o item de empréstimo (com botão Renovar)
            val itemView = inflater.inflate(R.layout.item_loan, loansContainer, false)

            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvDue = itemView.findViewById<TextView>(R.id.tvDue)
            val btnRenew = itemView.findViewById<Button>(R.id.btnRenew)

            tvTitle.text = loan.title
            tvDue.text = "Devolução: ${loan.due}"

            btnRenew.setOnClickListener {
                showRenewDialog(loan)
            }

            loansContainer.addView(itemView)
        }



        // 3) próximos eventos mockados
        val txtNextEvents = view.findViewById<TextView>(R.id.txtNextEvents)
        txtNextEvents.text = "Clube de Leitura – 20/11/2025\nPalestra: Bibliotecas do Futuro – 05/12/2025"

        // 4) recomendações (carrossel)
        val rv = view.findViewById<RecyclerView>(R.id.rvRecommendations)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = RecommendationAdapter(recs) { book ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment.new(book))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showRenewDialog(loan: Loan) {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_renew)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)

        d.findViewById<TextView>(R.id.tvTitleDialog)?.text = "Renovar \"${loan.title}\"?"

        d.findViewById<Button>(R.id.btnYes).setOnClickListener {
            // ação de renovar
            Toast.makeText(requireContext(), "Livro renovado: ${loan.title}", Toast.LENGTH_SHORT).show()
            d.dismiss()
        }

        d.findViewById<Button>(R.id.btnNo).setOnClickListener {
            Toast.makeText(requireContext(), "Renovação recusada para ${loan.title}", Toast.LENGTH_SHORT).show()
            d.dismiss()
        }



        d.show()
    }

}
