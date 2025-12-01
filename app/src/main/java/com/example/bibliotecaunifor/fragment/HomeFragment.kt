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
import com.example.bibliotecaunifor.models.RenewRentalRequest
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
    private val rentalApi by lazy { RetrofitClient.rentalApi }

    // livros recomendados carregados da API (pra abrir o detalhe depois)
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

        rvRecommendations.adapter = RecommendationsAdapter(emptyList()) { book ->
            openRecommendedBookDetail(book)
        }
    }

    private fun openRecommendedBookDetail(book: Book) {
        val fragment = BookDetailFragment.newInstance(book, false)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // id do container da MainActivity
            .addToBackStack(null)
            .commit()
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

    // ================== EMPRÉSTIMOS ==================

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
            val btnRenew = itemView.findViewById<Button>(R.id.btnRenew)

            tvTitle.text = rental.book?.title ?: "Título indisponível"
            tvDue.text = "Devolução: ${formatDate(rental.dueDate)}"

            Glide.with(itemView.context)
                .load(rental.book?.imageUrl)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.placeholder_book)
                .centerCrop()
                .into(imgCover)

            // DEVOLVER
            btnReturn.text = "Devolver"
            btnReturn.setOnClickListener {
                confirmBookReturn(rental)
            }

            // RENOVAR
            btnRenew.text = "Renovar"
            btnRenew.setOnClickListener {
                confirmRenewRental(rental)
            }

            loansList.addView(itemView)
        }
    }

    // ---------- RENOVAÇÃO ----------

    private fun confirmRenewRental(rental: Rental) {
        val title = rental.book?.title ?: "este livro"
        AlertDialog.Builder(requireContext())
            .setTitle("Renovar empréstimo")
            .setMessage("Deseja renovar o empréstimo de \"$title\" por mais 7 dias?")
            .setPositiveButton("Sim") { _, _ -> renewRental(rental) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun renewRental(rental: Rental) {
        val rentalId = rental.id
        if (rentalId.isNullOrEmpty()) {
            safeToast("Erro: empréstimo sem ID.")
            return
        }

        val body = RenewRentalRequest(additionalDays = 7)

        rentalApi.renewRental(rentalId, body)
            .enqueue(object : retrofit2.Callback<Rental> {
                override fun onResponse(
                    call: retrofit2.Call<Rental>,
                    response: retrofit2.Response<Rental>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val updated = response.body()!!
                        val novaData = formatDate(updated.dueDate)
                        safeToast("Empréstimo renovado até $novaData")
                        carregarDadosIniciais()
                    } else {
                        val msg = when (response.code()) {
                            400 -> "Não foi possível renovar este empréstimo."
                            404 -> "Empréstimo não encontrado."
                            else -> "Erro ao renovar. Tente novamente."
                        }
                        safeToast(msg)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Rental>, t: Throwable) {
                    safeToast("Falha de conexão ao renovar: ${t.message}")
                }
            })
    }

    // ---------- DEVOLUÇÃO ----------

    private fun confirmBookReturn(rental: Rental) {
        val title = rental.book?.title ?: "este livro"
        AlertDialog.Builder(requireContext())
            .setTitle("Devolver livro")
            .setMessage("Tem certeza que deseja devolver \"$title\"?")
            .setPositiveButton("Sim") { _, _ -> returnRental(rental) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun returnRental(rental: Rental) {
        val rentalId = rental.id
        if (rentalId.isNullOrEmpty()) {
            safeToast("Erro: empréstimo sem ID.")
            return
        }

        rentalApi.returnRental(rentalId)
            .enqueue(object : retrofit2.Callback<Rental> {
                override fun onResponse(
                    call: retrofit2.Call<Rental>,
                    response: retrofit2.Response<Rental>
                ) {
                    if (response.isSuccessful) {
                        safeToast("Livro devolvido com sucesso!")
                        carregarDadosIniciais()
                    } else {
                        safeToast("Erro ao devolver o livro (${response.code()})")
                    }
                }

                override fun onFailure(call: retrofit2.Call<Rental>, t: Throwable) {
                    safeToast("Sem conexão ao devolver: ${t.message}")
                }
            })
    }

    // ================== EVENTOS ==================

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

    // ================== RECOMENDAÇÕES ==================

    private fun loadRecommendations() {
        bookApi.getBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (!response.isSuccessful) return
                val books = response.body() ?: emptyList()
                val recomendados = books
                    .filter { it.availableCopies > 0 }
                    .shuffled()
                    .take(5)

                recommendedBooks = recomendados

                (rvRecommendations.adapter as? RecommendationsAdapter)
                    ?.updateBooks(recomendados)
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}
        })
    }

    private fun openRecommendedBookDetail(bookId: String) {
        val book = recommendedBooks.find { it.id == bookId }
        if (book == null) {
            safeToast("Livro não encontrado")
            return
        }

        val fragment = BookDetailFragment.newInstance(book, false)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)  // <-- aqui usa teu FrameLayout
            .addToBackStack(null)
            .commit()
    }

    // ================== UTIL ==================

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
