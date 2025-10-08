package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        // Edge-to-edge padding no root "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        // como esta é a tela de Perfil:
        bottom.selectedItemId = R.id.nav_profile

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // se tiver HomeActivity, navegue pra ela; se não, só consuma
                    true
                }
                R.id.nav_catalog -> {
                    startActivity(Intent(this, CatalogActivity::class.java))
                    overridePendingTransition(0, 0) // sem animação entre telas
                    true
                }

                R.id.nav_profile -> true // já está aqui
                else -> false
            }
        }
    }
}
