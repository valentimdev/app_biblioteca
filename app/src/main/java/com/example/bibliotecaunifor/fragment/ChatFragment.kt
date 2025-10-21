// ChatFragment.kt
package com.example.bibliotecaunifor.fragment
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R

class ChatFragment : Fragment(R.layout.activity_chat) {
    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }
}
