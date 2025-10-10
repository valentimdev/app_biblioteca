// ProfileFragment.kt
package com.example.bibliotecaunifor
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

// Pode reaproveitar seu XML de perfil:
class ProfileFragment : Fragment(R.layout.activity_perfil_usuario) {
    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("PERFIL", showBack = false)
    }
}
