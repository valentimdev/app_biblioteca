package com.example.bibliotecaunifor

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.NotificacoesAdapter
import com.example.bibliotecaunifor.notifications.AppNotification
import com.example.bibliotecaunifor.notifications.NotificationStore
import com.google.android.material.chip.Chip

class NotificacoesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacoesAdapter

    private lateinit var chipTodas: Chip
    private lateinit var chipNaoLidas: Chip

    private var allNotifications: List<AppNotification> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notificacoes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.toolbar3))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        recyclerView = findViewById(R.id.recyclerViewNotificacoes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        chipTodas = findViewById(R.id.chipTodas)
        chipNaoLidas = findViewById(R.id.chipNaoLidas)

        adapter = NotificacoesAdapter(emptyList()) { notif ->
            mostrarDetalheNotificacao(notif)
        }
        recyclerView.adapter = adapter

        carregarNotificacoes()

        chipTodas.setOnClickListener {
            aplicarFiltro(todas = true)
        }

        chipNaoLidas.setOnClickListener {
            aplicarFiltro(todas = false)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarNotificacoes()
    }

    private fun carregarNotificacoes() {
        allNotifications = NotificationStore.getNotifications(this)
            .sortedByDescending { it.dateMillis }

        adapter.updateList(allNotifications)
    }

    private fun aplicarFiltro(todas: Boolean) {
        val filtradas = if (todas) {
            allNotifications
        } else {
            allNotifications.filter { !it.read }
        }
        adapter.updateList(filtradas)
    }

    private fun mostrarDetalheNotificacao(notif: AppNotification) {
        // marca como lida
        NotificationStore.markAsRead(this, notif.id)
        carregarNotificacoes()

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalhe_notificacao)

        val tvAssuntoPopup = dialog.findViewById<TextView>(R.id.tvAssuntoPopup)
        val tvMensagemPopup = dialog.findViewById<TextView>(R.id.tvMensagemPopup)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

        tvAssuntoPopup.text = "ASSUNTO: ${notif.title}"
        tvMensagemPopup.text = "MENSAGEM: ${notif.message}"

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
