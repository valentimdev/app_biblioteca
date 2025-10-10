// EventsFragment.kt
package com.example.bibliotecaunifor
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class EventsFragment : Fragment(R.layout.activity_eventos) {
    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("EVENTOS", showBack = false)
    }
}
