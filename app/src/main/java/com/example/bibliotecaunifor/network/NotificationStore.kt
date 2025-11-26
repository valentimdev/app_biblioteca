package com.example.bibliotecaunifor.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NotificationStore {

    private const val PREFS_NAME = "notificacoes_prefs"
    private const val KEY_LIST = "notificacoes_list"

    fun addNotification(context: Context, notification: AppNotification) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_LIST, "[]") ?: "[]"
        val array = JSONArray(jsonStr)

        val obj = JSONObject().apply {
            put("id", notification.id)
            put("title", notification.title)
            put("message", notification.message)
            put("dateMillis", notification.dateMillis)
            put("type", notification.type ?: JSONObject.NULL)
            put("read", notification.read)
        }

        array.put(obj)

        prefs.edit().putString(KEY_LIST, array.toString()).apply()
    }

    fun getNotifications(context: Context): List<AppNotification> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_LIST, "[]") ?: "[]"
        val array = JSONArray(jsonStr)

        val list = mutableListOf<AppNotification>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                AppNotification(
                    id = obj.optLong("id"),
                    title = obj.optString("title"),
                    message = obj.optString("message"),
                    dateMillis = obj.optLong("dateMillis"),
                    type = if (obj.has("type") && !obj.isNull("type")) obj.getString("type") else null,
                    read = obj.optBoolean("read", false)
                )
            )
        }
        return list
    }

    fun markAsRead(context: Context, id: Long) {
        updateNotification(context, id) { it.copy(read = true) }
    }

    private fun updateNotification(
        context: Context,
        id: Long,
        transform: (AppNotification) -> AppNotification
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_LIST, "[]") ?: "[]"
        val array = JSONArray(jsonStr)

        val newArray = JSONArray()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val current = AppNotification(
                id = obj.optLong("id"),
                title = obj.optString("title"),
                message = obj.optString("message"),
                dateMillis = obj.optLong("dateMillis"),
                type = if (obj.has("type") && !obj.isNull("type")) obj.getString("type") else null,
                read = obj.optBoolean("read", false)
            )

            val updated = if (current.id == id) transform(current) else current

            val newObj = JSONObject().apply {
                put("id", updated.id)
                put("title", updated.title)
                put("message", updated.message)
                put("dateMillis", updated.dateMillis)
                put("type", updated.type ?: JSONObject.NULL)
                put("read", updated.read)
            }

            newArray.put(newObj)
        }

        prefs.edit().putString(KEY_LIST, newArray.toString()).apply()
    }
}
