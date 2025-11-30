package com.example.bibliotecaunifor.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.ConfiguracoesActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.fragment.AdminEventsFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomAdmin: BottomNavigationView

    // 👇 callback que o fragment pode registrar
    private var onAddClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        toolbar = findViewById(R.id.toolbar)
        bottomAdmin = findViewById(R.id.bottomAdmin)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Painel Administrativo"
        }

        if (savedInstanceState == null) {
            replace(AdminHomeFragment())
            bottomAdmin.selectedItemId = R.id.nav_admin_dashboard
        }

        bottomAdmin.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_admin_dashboard -> {
                    toolbar.menu.clear()
                    toolbar.title = "Painel Administrativo"
                    replace(AdminHomeFragment())
                }
                R.id.nav_admin_users -> {
                    toolbar.menu.clear()
                    toolbar.title = "Gestão de Usuários"
                    replace(GestaoFragment())
                }
                R.id.nav_admin_catalog -> {
                    toolbar.title = "Catálogo (Admin)"
                    // mostra o botão de adicionar no catálogo
                    toolbar.menu.clear()
                    replace(CatalogAdminFragment())
                }
                R.id.nav_admin_events -> {
                    toolbar.menu.clear()
                    toolbar.title = "Eventos"
                    replace(AdminEventsFragment())
                }
                R.id.nav_admin_settings -> {
                    startActivity(Intent(this, ConfiguracoesActivity::class.java))
                }
            }
            true
        }
    }

    private fun replace(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_container, f)
            .commit()
    }

    // 👇 ISSO é o que o teu fragment está tentando chamar
    fun setAddButtonListener(listener: () -> Unit) {
        onAddClick = listener
    }
}
