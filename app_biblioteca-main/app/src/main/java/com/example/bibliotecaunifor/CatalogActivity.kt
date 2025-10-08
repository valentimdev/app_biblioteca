package com.example.bibliotecaunifor
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.model.Book
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class CatalogActivity : AppCompatActivity() {

    private lateinit var adapter: BookAdapter
    private lateinit var allBooks: List<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BibliotecaUnifor)
        setContentView(R.layout.activity_catalog)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_bell -> { Toast.makeText(this, "Notificações", Toast.LENGTH_SHORT).show(); true }
                R.id.action_filter -> { Toast.makeText(this, "Filtro", Toast.LENGTH_SHORT).show(); true }
                else -> false
            }
        }

        val rv = findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(this)

        allBooks = demoBooks()
        adapter = BookAdapter(allBooks) { openDetail(it) }
        rv.adapter = adapter

        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.doAfterTextChanged { text ->
            val q = text.toString().trim().lowercase()
            val filtered = allBooks.filter {
                it.title.lowercase().contains(q) ||
                        it.author.lowercase().contains(q) ||
                        it.genre.lowercase().contains(q)
            }
            rv.adapter = BookAdapter(filtered) { openDetail(it) }
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottom.selectedItemId = R.id.nav_catalog
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> true
                else -> { Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show(); true }
            }
        }
        val bottomPerfil = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomPerfil.selectedItemId = R.id.nav_catalog

        bottomPerfil.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, PerfilUsuario::class.java))
                    overridePendingTransition(0, 0) // sem animação
                    finish() // opcional: evita empilhar telas
                    true
                }
                R.id.nav_catalog -> true
                else -> false
            }
        }
    }

    private fun openDetail(book: Book) {
        val i = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("title", book.title)
            putExtra("author", book.author)
            putExtra("genre", book.genre)
            putExtra("year", book.year)
            putExtra("qty", book.quantity)
            putExtra("synopsis", book.synopsis)
        }
        startActivity(i)
    }

    private fun demoBooks() = listOf(
        Book("1","O Nome do Vento","Patrick Rothfuss","Fantasia",2007,5,"A história de Kvothe..."),
        Book("2","Dom Casmurro","Machado de Assis","Clássico",1899,2,"Ciúmes, memórias e Capitu."),
        Book("3","1984","George Orwell","Distopia",1949,4,"O Estado onipresente...")
    )
}
