package com.example.bibliotecaunifor.fragment

import com.example.bibliotecaunifor.models.EventResponse
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.Rental
import com.example.bibliotecaunifor.adapters.RecommendationsAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.UserResponse
import com.example.bibliotecaunifor.utils.AuthUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.mapNotNull

class HomeFragment : Fragment() {

    private lateinit var txtWelcome: TextView
    private lateinit var loansList: LinearLayout
    private lateinit var txtNextEvents: TextView
    private lateinit var rvRecommendations: androidx.recyclerview.widget.RecyclerView

    private val userApi by lazy { RetrofitClient.userApi }
    private val bookApi by lazy { RetrofitClient.bookApi }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bindViews(view)
        setupRecyclerView()

        loadUserData()        // pega /users/me -> nome, empréstimos, eventos
        loadRecommendations() // pega livros recomendados (passando token)

        return view
    }

    private fun bindViews(view: android.view.View) {
        txtWelcome = view.findViewById(R.id.txtWelcome)
        loansList = view.findViewById(R.id.loansList)
        txtNextEvents = view.findViewById(R.id.txtNextEvents)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
    }

    private fun setupRecyclerView() {
        rvRecommendations.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvRecommendations.adapter = RecommendationsAdapter(emptyList()) { bookId ->
            Toast.makeText(context, "Recomendação selecionada: $bookId", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // 1) Carrega /users/me → Nome, Empréstimos, Eventos
    // ============================================================
    private fun loadUserData() {
        userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful) {
                    showError("Erro ao carregar usuário", response.code())
                    return
                }

                val user = response.body() ?: run {
                    showError("Corpo de usuário vazio")
                    return
                }

                Log.d("HomeFragment", "User recebido: ${user.name}")
                txtWelcome.text = "Olá ${user.name}, bem-vindo!"
                populateLoans(user.rentals)
                populateEvents(user.events)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                showError("Falha ao buscar usuário: ${t.message}")
            }
        })
    }

    fun reloadHome() {
        loadUserData()
        loadRecommendations()
    }

    private fun populateLoans(rentals: List<Rental>) {
        loansList.removeAllViews()
        if (rentals.isEmpty()) {
            addEmptyView("Nenhum empréstimo ativo")
            return
        }

        rentals.forEach { rental ->
            val itemView = android.view.LayoutInflater.from(context).inflate(R.layout.item_loan, loansList, false)
            itemView.findViewById<TextView>(R.id.tvTitle).text = rental.book.title
            itemView.findViewById<TextView>(R.id.tvDue).text = "Devolução: ${formatDate(rental.dueDate)}"

            itemView.findViewById<Button>(R.id.btnReturn).setOnClickListener {
                confirmBookReturn(rental.book.id, rental.book.title)
            }

            loansList.addView(itemView)
        }
    }

    private fun confirmBookReturn(bookId: String, title: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Devolver")
            .setMessage("Deseja devolver \"$title\"?")
            .setPositiveButton("Sim") { _, _ -> returnBook(bookId) }
            .setNegativeButton("Não", null)
            .show()
    }

    // ============================================================
    // 3) returnBook -> chama bookApi.returnBook(bookId, token)
    // ============================================================
    private fun returnBook(bookId: String) {
        val token = getAuthToken() ?: return showError("Faça login para devolver livro")
        // token já tem o prefixo "Bearer " conforme sua função getAuthToken()
        bookApi.returnBook(bookId, token).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(context, "Livro devolvido!", Toast.LENGTH_SHORT).show()
                    loadUserData()
                } else {
                    showError("Erro ao devolver livro", response.code())
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                showError("Falha na rede ao devolver: ${t.message}")
            }
        })
    }

    // ============================================================
    // 4) Próximos Eventos (recebidos via /users/me)
    // ============================================================
    private fun populateEvents(events: List<EventResponse>) {
        if (events.isEmpty()) {
            txtNextEvents.text = "Nenhum evento próximo"
            return
        }

        val upcoming = events
            .mapNotNull { ev -> parseDate(ev.startTime)?.let { ev to it.time } }
            .filter { it.second > System.currentTimeMillis() }
            .sortedBy { it.second }
            .take(3)

        if (upcoming.isEmpty()) {
            txtNextEvents.text = "Nenhum evento próximo"
            return
        }

        txtNextEvents.text = upcoming.joinToString("\n") { (ev, ts) ->
            "${ev.title} - ${formatDateTime(ts)}"
        }
    }

    // ============================================================
    // 5) Recomendações (passando token se bookApi exigir)
    // ============================================================
    private fun loadRecommendations() {
        val token = getAuthToken() ?: return // se precisar do token, força login
        bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (!response.isSuccessful) {
                    Log.e("HomeFragment", "Recomendações erro HTTP ${response.code()}")
                    return
                }
                val books = response.body() ?: emptyList()
                val recommendations = books.filter { it.availableCopies > 0 }.shuffled().take(5)
                (rvRecommendations.adapter as? RecommendationsAdapter)?.updateBooks(recommendations)
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                Log.e("HomeFragment", "Erro recomendação: ${t.message}")
            }
        })
    }

    // ============================================================
    // UTIL
    // ============================================================
    private fun getAuthToken(): String? {
        val ctx = context ?: return null
        val token = AuthUtils.getToken(ctx) // retorna token puro, ex "eyJ..."
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
        return SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault()).format(Date(timestamp))
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
        val complete = if (code != null) "$msg (HTTP $code)" else msg
        Toast.makeText(context, complete, Toast.LENGTH_LONG).show()
    }
}
