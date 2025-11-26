// utils/ToastUtils.kt
package com.example.bibliotecaunifor.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

object ToastUtils {
    fun show(context: Context?, message: String) {
        if (context != null && message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun Fragment.showToast(message: String) {
        if (isAdded && context != null && message.isNotBlank()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}