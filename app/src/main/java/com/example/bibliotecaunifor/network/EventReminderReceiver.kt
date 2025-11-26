package com.example.bibliotecaunifor.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Evento"
        val message = "Seu evento \"$title\" acontece em 24 horas."

        NotificationHelper.showNotification(
            context,
            "Lembrete de evento",
            message
        )
    }
}
