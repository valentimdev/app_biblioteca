package com.example.bibliotecaunifor.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.bibliotecaunifor.Evento
import com.example.bibliotecaunifor.R
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    private const val CHANNEL_ID = "eventos_channel"
    private const val CHANNEL_NAME = "Eventos"
    private const val CHANNEL_DESC = "Notificações de eventos da biblioteca"

    private val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
        type: String? = null
    ) {
        createChannel(context)

        // 👉 Sempre salva na lista interna (tela de Notificações), independente da permissão
        NotificationStore.addNotification(
            context,
            AppNotification(
                id = System.currentTimeMillis(),
                title = title,
                message = message,
                dateMillis = System.currentTimeMillis(),
                type = type,
                read = false
            )
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_marker) // qualquer ícone que exista no projeto
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // ✅ Checagem de permissão para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // Permissão não concedida → não chama notify(), mas já salvamos na NotificationStore
                return
            }
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * Agenda um lembrete 24h antes do evento.
     */
    fun scheduleEventReminder24hBefore(context: Context, evento: Evento) {
        val date = try {
            formatoISO.parse(evento.startTime)
        } catch (e: Exception) {
            null
        } ?: return

        val reminderTime = date.time - 24L * 60L * 60L * 1000L // 24h antes

        // se já passou, não agenda nada
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("eventId", evento.id)
            putExtra("title", evento.title)
            putExtra("startTime", evento.startTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            evento.id.hashCode(), // requestCode único por evento
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }

    /**
     * Cancela o lembrete de 24h caso o usuário cancele a inscrição.
     */
    fun cancelEventReminder(context: Context, eventId: String) {
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
