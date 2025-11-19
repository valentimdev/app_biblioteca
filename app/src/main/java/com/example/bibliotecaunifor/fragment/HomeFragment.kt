package com.example.bibliotecaunifor.fragment

import com.example.bibliotecaunifor.models.EventResponse
import android.os.Bundle
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
import com.example.bibliotecaunifor.api.EventApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

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
        loadUserData()
        loadRecommendations()
        loadEvents()
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

    private fun loadUserData() {
        userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful) {
                    showError("Erro ao carregar usuário", response.code())
                    return
                }
                val user = response.body() ?: return showError("Corpo de usuário vazio")
                txtWelcome.text = "Olá ${user.name}, bem-vindo!"
                populateLoans(user.rentals)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                showError("Falha ao buscar usuário: ${t.message}")
            }
        })
    }

    private fun loadEvents() {
        Thread {
            val eventos = EventApi.fetchEventos()

            val mapped = eventos.map {
                EventResponse(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    location = it.location ?: "",
                    imageUrl = it.imageUrl,
                    lecturers = it.lecturers,
                    seats = it.seats,
                    isDisabled = it.isDisabled,
                    adminId = it.adminId,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }

            activity?.runOnUiThread {
                populateEvents(mapped)
            }
        }.start()
    }

    fun reloadHome() {
        loadUserData()
        loadRecommendations()
        loadEvents()
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

    private fun returnBook(bookId: String) {
        val token = getAuthToken() ?: return showError("Faça login para devolver livro")
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

    private fun populateEvents(events: List<EventResponse>) {
        if (events.isEmpty()) {
            txtNextEvents.text = "Nenhum evento próximo"
            return
        }

        val proximos = events
            .mapNotNull { ev ->
                parseDate(ev.startTime)?.let { date -> ev to date.time }
            }
            .filter { it.second > System.currentTimeMillis() }
            .sortedBy { it.second }
            .take(2)

        if (proximos.isEmpty()) {
            txtNextEvents.text = "Nenhum evento próximo"
            return
        }

        val texto = proximos.joinToString("\n\n") { (ev, ts) ->
            "${ev.title}\n${formatDateTime(ts)}"
        }

        txtNextEvents.text = texto
    }

    private fun loadRecommendations() {
        val token = getAuthToken() ?: return
        bookApi.getBooks(token).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (!response.isSuccessful) return
                val books = response.body() ?: emptyList()
                val recommendations = books.filter { it.availableCopies > 0 }.shuffled().take(5)
                (rvRecommendations.adapter as? RecommendationsAdapter)?.updateBooks(recommendations)
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun getAuthToken(): String? {
        val ctx = context ?: return null
        val token = AuthUtils.getToken(ctx)
        return if (token != null) "Bearer $token" else null
    }

    private fun parseDate(iso: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        )

        formats.forEach { pattern ->
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(iso)
            } catch (_: Exception) {}
        }
        return null
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
