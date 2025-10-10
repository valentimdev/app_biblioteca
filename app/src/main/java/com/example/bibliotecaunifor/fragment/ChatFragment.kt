// ChatFragment.kt
package com.example.bibliotecaunifor
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class ChatFragment : Fragment(R.layout.activity_chat) {
    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("CHAT", showBack = false)
    }
}
