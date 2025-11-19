package com.example.bibliotecaunifor.utils

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

object AuthUtils {
    private const val TOKEN_KEY = "jwt_token"
    private const val ROLE_KEY = "user_role"

    fun saveToken(context: Context, token: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(TOKEN_KEY, null)
    }

    fun saveRole(context: Context, role: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(ROLE_KEY, role).apply()
    }

    fun getRole(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(ROLE_KEY, null)
    }

    fun clear(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit {
            remove(TOKEN_KEY)
            remove(ROLE_KEY)
        }
    }

    private const val NAME_KEY = "user_name"

    fun saveUserName(context: Context, name: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(NAME_KEY, name).apply()
    }

    fun getUserName(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(NAME_KEY, null)
    }
}
