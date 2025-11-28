package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecaunifor.Book
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.RecommendationsAdapter
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.EventDto
import com.example.bibliotecaunifor.models.Rental
import com.example.bibliotecaunifor.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var txtWelcome: TextView
    private lateinit var loansList: LinearLayout
    private lateinit var txtNextEvents: TextView
    private lateinit var rvRecommendations: RecyclerView

    private val userApi by lazy { RetrofitClient.userApi }
    private val bookApi by lazy { RetrofitClient.bookApi }

    private var recommendedBooks: List<Book> = emptyList()
    private var userRentedBookIds: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bindViews(view)
        setupRecyclerView()
        carregarDadosIniciais()
        return view
    }

    private fun bindViews(view: View) {
        txtWelcome = view.findViewById(R.id.txtWelcome)
        loansList = view.findViewById(R.id.loansList)
        txtNextEvents = view.findViewById(R.id.txtNextEvents)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
    }

    private fun setupRecyclerView() {
        rvRecommendations.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        rvRecommendations.adapter = RecommendationsAdapter(emptyList()) { bookId ->
            val book = recommendedBooks.firstOrNull { it.id == bookId } ?: return@RecommendationsAdapter

            parentFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    BookDetailFragment.newInstance(book, userRentedBookIds.contains(book.id)),
                    "book_detail"
                )
                .hide(this)
                .addToBackStack("book_detail")
                .commit()
        }
    }

    private fun carregarDadosIniciais() {
        userApi.getMe().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    safeToast("Erro ao carregar dados do usuário")
                    return
                }

                val user = response.body()!!

                txtWelcome.text = "Olá ${user.name}, bem-vindo!"

                val emprestimosAtivos = user.rentals.filter { it.returnDate == null }
                populateLoans(emprestimosAtivos)

                userRentedBookIds = emprestimosAtivos.mapNotNull { it.bookId }

                val eventosProximos = user.events
                    .mapNotNull { it.toEvento() }
                    .filter { isFutureEvent(it.startTime) }
                    .sortedBy { parseDate(it.startTime)?.time ?: Long.MAX_VALUE }
                    .take(2)

                populateEvents(eventosProximos)

                loadRecommendations()
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                safeToast("Sem conexão com o servidor")
            }
        })
    }

    private fun EventDto.toEvento(): Evento = Evento(
        id = id,
        title = title,
        description = description,
        startTime = eventStartTime,
        endTime = eventEndTime,
        location = location,
        imageUrl = imageUrl,
        lecturers = lecturers,
        seats = seats,
        registrationStartTime = registrationStartTime,
        registrationEndTime = registrationEndTime,
        isDisabled = isDisabled,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )

    private fun isFutureEvent(dateString: String): Boolean {
        return try {
            val date = parseDate(dateString) ?: return false
            date.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun populateLoans(rentals: List<Rental>) {
        loansList.removeAllViews()
        if (rentals.isEmpty()) {
            addEmptyView("Nenhum empréstimo ativo")
            return
        }

        rentals.forEach { rental ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_loan, loansList, false)

            val imgCover = itemView.findViewById<ImageView>(R.id.imgCover)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvDue = itemView.findViewById<TextView>(R.id.tvDue)
            val btnReturn = itemView.findViewById<Button>(R.id.btnReturn)

            tvTitle.text = rental.book?.title ?: "Título indisponível"
            tvDue.text = "Devolução: ${formatDate(rental.dueDate)}"

            Glide.with(itemView.context)
                .load(rental.book?.imageUrl)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.placeholder_book)
                .centerCrop()
                .into(imgCover)

            btnReturn.setOnClickListener {
                rental.book?.let { book ->
                    confirmBookReturn(book.id, book.title ?: "Livro")
                }
            }

            loansList.addView(itemView)
        }
    }

    private fun confirmBookReturn(bookId: String, title: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Devolver livro")
            .setMessage("Tem certeza que deseja devolver \"$title\"?")
            .setPositiveButton("Sim") { _, _ -> returnBook(bookId) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun returnBook(bookId: String) {
        bookApi.returnBook(bookId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    safeToast("Livro devolvido com sucesso!")
                    carregarDadosIniciais()
                } else {
                    safeToast("Erro ao devolver o livro")
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                safeToast("Sem conexão")
            }
        })
    }

    private fun populateEvents(eventos: List<Evento>) {
        if (eventos.isEmpty()) {
            txtNextEvents.text = "Nenhum evento próximo"
            return
        }

        val texto = eventos.joinToString("\n\n") { evento ->
            val horaFormatada = parseDate(evento.startTime)?.let {
                SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault()).format(it)
            } ?: "Horário não disponível"
            "${evento.title}\n$horaFormatada"
        }
        txtNextEvents.text = texto
    }

    private fun loadRecommendations() {
        bookApi.getBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (!response.isSuccessful) return
                val books = response.body() ?: emptyList()
                recommendedBooks = books.filter { it.availableCopies > 0 }.shuffled().take(5)
                (rvRecommendations.adapter as? RecommendationsAdapter)?.updateBooks(recommendedBooks)
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun formatDate(iso: String?): String {
        if (iso.isNullOrEmpty()) return "Data não informada"
        return try {
            val date = parseDate(iso) ?: return iso.take(10)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            iso.take(10)
        }
    }

    private fun addEmptyView(message: String) {
        val tv = TextView(requireContext()).apply {
            text = message
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            textSize = 15f
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER
        }
        loansList.addView(tv)
    }

    private fun safeToast(message: String) {
        if (isAdded && context != null && message.isNotBlank()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    fun reload() {
        if (isAdded) {
            carregarDadosIniciais()
        }
    }
}
