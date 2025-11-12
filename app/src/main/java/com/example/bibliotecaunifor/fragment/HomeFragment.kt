package com.example.bibliotecaunifor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.RecommendationsAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.EventDto
import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.services.EventService
import com.example.bibliotecaunifor.utils.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val bookApi by lazy { RetrofitClient.bookApi }
    private lateinit var txtWelcome: TextView
    private lateinit var loansList: LinearLayout
    private lateinit var txtNextEvents: TextView
    private lateinit var rvRecommendations: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bindViews(view)
        setupRecyclerView()
        loadData()
        return view
    }

    private fun bindViews(view: View) {
        txtWelcome = view.findViewById(R.id.txtWelcome)
        loansList = view.findViewById(R.id.loansList)
        txtNextEvents = view.findViewById(R.id.txtNextEvents)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
    }

    private fun setupRecyclerView() {
        rvRecommendations.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvRecommendations.adapter = RecommendationsAdapter(emptyList()) { bookId ->
            Toast.makeText(context, "Recomendação: $bookId", Toast.LENGTH_SHORT).show()
            // Abre detalhe se quiser
        }
    }

    private fun loadData() {
        loadWelcome()
        loadMyLoans()
        loadNextEvents()
        loadRecommendations()
    }

    private fun loadWelcome() {
        txtWelcome.text = "OLA FULANO, BEM VINDO"
    }

    // === EMPRÉSTIMOS ===
    fun loadMyLoans() {
        val token = getAuthToken() ?: return showError("Faça login")

        Log.d("HomeFragment", "Carregando empréstimos...")

        RetrofitClient.bookApi.getMyRentals(token).enqueue(object : Callback<List<Rental>> {
            override fun onResponse(call: Call<List<Rental>>, response: Response<List<Rental>>) {
                Log.d("HomeFragment", "Response: ${response.code()}")

                if (response.isSuccessful) {
                    val rentals = response.body() ?: emptyList()
                    Log.d("HomeFragment", "Rentals recebidos: ${rentals.size}")
                    rentals.forEach { r ->
                        Log.d("HomeFragment", "Rental: ${r.book.title}, returnDate: ${r.returnDate}")
                    }

                    populateLoans(rentals)
                } else {
                    showError("Erro: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Rental>>, t: Throwable) {
                Log.e("HomeFragment", "Falha na rede", t)
                showError("Sem conexão")
            }
        })
    }

    private fun populateLoans(rentals: List<Rental>) {
        loansList.removeAllViews()
        val validRentals = rentals.filter {
            it.book.id.isNotBlank() && it.book.title.isNotBlank()
        }

        if (validRentals.isEmpty()) {
            addEmptyView("Nenhum empréstimo ativo")
            return
        }

        validRentals.forEach { rental ->
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_loan, loansList, false)
            itemView.findViewById<TextView>(R.id.tvTitle).text = rental.book.title
            itemView.findViewById<TextView>(R.id.tvDue).text = "Devolução: ${formatDate(rental.dueDate)}"

            itemView.findViewById<Button>(R.id.btnReturn).setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Devolver")
                    .setMessage("Devolver \"${rental.book.title}\"?")
                    .setPositiveButton("Sim") { _, _ -> returnBook(rental.book.id) }
                    .setNegativeButton("Não", null)
                    .show()
            }

            loansList.addView(itemView)
        }
    }

    private fun returnBook(bookId: String) {
        val token = getAuthToken() ?: return showError("Token inválido")

        // Primeiro verifica se o livro existe
        RetrofitClient.bookApi.getBookById(bookId, token).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful && response.body() != null) {
                    // Livro existe → devolve
                    actuallyReturnBook(bookId, token)
                } else {
                    Toast.makeText(context, "Livro não encontrado. Empréstimo removido.", Toast.LENGTH_LONG).show()
                    loadMyLoans() // Recarrega sem o livro deletado
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                showError("Sem conexão")
            }
        })
    }

    private fun actuallyReturnBook(bookId: String, token: String) {
        RetrofitClient.bookApi.returnBook(bookId, token)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        Toast.makeText(context, "Devolvido com sucesso!", Toast.LENGTH_SHORT).show()
                        loadMyLoans()
                    } else {
                        showError("Falha ao devolver")
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    showError("Sem conexão")
                }
            })
    }

    private fun renewLoan(bookId: String) {
        val token = getAuthToken() ?: return showError("Token inválido")
        val dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(dueDate.time)

        val body = mapOf("dueDate" to isoDate)
        bookApi.rentBook(bookId, body, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(context, "Renovado com sucesso!", Toast.LENGTH_SHORT).show()
                    loadMyLoans()
                } else {
                    Toast.makeText(context, "Não foi possível renovar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                showError("Erro ao renovar")
            }
        })
    }

    // === EVENTOS (HttpURLConnection) ===
    private fun loadNextEvents() {
        Thread {
            val token = getAuthToken()
            val events = EventService.getAllEvents(token)
            val upcoming = events
                .mapNotNull { event ->
                    parseDate(event.startTime)?.let { date -> event to date }
                }
                .filter { (_, date) -> date.time > System.currentTimeMillis() }
                .sortedBy { (_, date) -> date.time }
                .take(3)

            activity?.runOnUiThread {
                if (upcoming.isEmpty()) {
                    txtNextEvents.text = "Nenhum evento próximo"
                } else {
                    txtNextEvents.text = upcoming.joinToString("\n") { (event, date) ->
                        "${event.title} - ${formatDateTime(date.time)}"
                    }
                }
            }
        }.start()
    }

    // === RECOMENDAÇÕES ===
    private fun loadRecommendations() {
        val token = getAuthToken() ?: return
        RetrofitClient.bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    val available = (response.body() ?: emptyList()).filter { it.availableCopies > 0 }
                    val recommendations = available.shuffled().take(5)
                    (rvRecommendations.adapter as? RecommendationsAdapter)?.updateBooks(recommendations)
                }
            }
            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                // Não trava
            }
        })
    }

    // === UTIL ===
    private fun getAuthToken(): String? {
        val context = context ?: return null
        val token = AuthUtils.getToken(context)
        return if (token != null) "Bearer $token" else null
    }

    private fun parseDate(iso: String): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(iso)
        } catch (e: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.parse(iso)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun formatDate(iso: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = sdf.parse(iso) ?: return iso
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            iso
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun addEmptyView(message: String) {
        val tv = TextView(context).apply {
            text = message
            setTextColor(resources.getColor(android.R.color.darker_gray))
            textSize = 14f
        }
        loansList.addView(tv)
    }

    private fun showError(msg: String, code: Int? = null) {
        val fullMsg = if (code != null) "$msg (HTTP $code)" else msg
        Toast.makeText(context, fullMsg, Toast.LENGTH_LONG).show()
    }
}