package com.example.bibliotecaunifor
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class BookDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BibliotecaUnifor)
        setContentView(R.layout.activity_book_detail)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<TextView>(R.id.tvBookTitle).text = intent.getStringExtra("title")
        findViewById<TextView>(R.id.tvAuthor).text      = intent.getStringExtra("author")
        findViewById<TextView>(R.id.tvGenre).text       = intent.getStringExtra("genre")
        findViewById<TextView>(R.id.tvYear).text        = intent.getIntExtra("year", 0).toString()
        findViewById<TextView>(R.id.tvQty).text         = intent.getIntExtra("qty", 0).toString()
        findViewById<TextView>(R.id.tvSynopsis).text    = intent.getStringExtra("synopsis")

        findViewById<MaterialButton>(R.id.btnRent).setOnClickListener {
            Toast.makeText(this, "Solicitação de aluguel enviada!", Toast.LENGTH_SHORT).show()
        }
    }
}
