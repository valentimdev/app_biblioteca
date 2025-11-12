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
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.utils.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {

    data class Loan(val title: String, val due: String)

    private val loans = listOf(
        Loan("MERIDIANO DE SANGUE", "11/11/2025"),
        Loan("Authenticgames: vivendo uma vida autêntica", "22/11/2025"),
        Loan("Mourinho (the special one)", "15/11/2026")
    )

    private val recs = mutableListOf<Book>()
    private lateinit var recAdapter: RecommendationAdapter

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Lista de empréstimos
        val loansContainer = view.findViewById<LinearLayout>(R.id.loansList)
        val inflater = LayoutInflater.from(requireContext())
        loans.forEach { loan ->
            val itemView = inflater.inflate(R.layout.item_loan, loansContainer, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvDue = itemView.findViewById<TextView>(R.id.tvDue)
            val btnRenew = itemView.findViewById<Button>(R.id.btnRenew)

            tvTitle.text = loan.title
            tvDue.text = "Devolução: ${loan.due}"

            btnRenew.setOnClickListener { showRenewDialog(loan) }

            loansContainer.addView(itemView)
        }

        // 2) Próximos eventos mock
        val txtNextEvents = view.findViewById<TextView>(R.id.txtNextEvents)
        txtNextEvents.text = "Clube de Leitura – 20/11/2025\nPalestra: Bibliotecas do Futuro – 05/12/2025"

        // 3) Recomendações (RecyclerView horizontal)
        val rv = view.findViewById<RecyclerView>(R.id.rvRecommendations)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recAdapter = RecommendationAdapter(recs) { book ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment.newInstance(book, false))
                .addToBackStack(null)
                .commit()
        }
        rv.adapter = recAdapter

        // 4) Busca as recomendações do backend
        fetchRecommendedBooks()
    }

    private fun fetchRecommendedBooks() {
        val tokenStr = AuthUtils.getToken(requireContext())
        if (tokenStr.isNullOrEmpty()) {
            android.util.Log.e("HomeFragment", "Token nulo!")
            return
        }
        val token = "Bearer $tokenStr"

        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                android.util.Log.d("HomeFragment", "Response code: ${response.code()}")
                android.util.Log.d("HomeFragment", "Body: ${response.body()}")

                if (response.isSuccessful) {
                    recs.clear()
                    val books = response.body()?.filter { it.availableCopies > 0 } ?: emptyList()
                    recs.addAll(books)
                    recAdapter.notifyDataSetChanged()
                } else {
                    android.util.Log.e(
                        "HomeFragment",
                        "Erro ao buscar livros: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(requireContext(), "Erro ao carregar recomendações", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                android.util.Log.e("HomeFragment", "Falha na requisição", t)
                Toast.makeText(requireContext(), "Erro ao carregar recomendações", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRenewDialog(loan: Loan) {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_renew)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)

        d.findViewById<TextView>(R.id.tvTitleDialog)?.text = "Renovar \"${loan.title}\"?"

        d.findViewById<Button>(R.id.btnYes).setOnClickListener {
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
