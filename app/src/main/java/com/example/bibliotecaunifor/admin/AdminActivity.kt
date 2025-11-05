package com.example.bibliotecaunifor.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import br.unifor.biblioteca.admin.GestaoFragment
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.fragment.AdminEventsFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomAdmin: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        toolbar = findViewById(R.id.toolbar)
        bottomAdmin = findViewById(R.id.bottomAdmin)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Painel Administrativo"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // carrega tela inicial
        if (savedInstanceState == null) {
            replace(AdminHomeFragment())
            bottomAdmin.selectedItemId = R.id.nav_admin_dashboard
        }

        bottomAdmin.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_admin_dashboard -> {
                    toolbar.title = "Painel Administrativo"
                    replace(AdminHomeFragment())
                }
                R.id.nav_admin_users -> {
                    toolbar.title = "Gestão de Usuários"
                    replace(GestaoFragment()) // se você já tem esse fragment de gestão
                }
                R.id.nav_admin_catalog -> {
                    toolbar.title = "Catálogo (Admin)"
                    replace(AdminCatalogFragment())
                }
                R.id.nav_admin_events -> {
                    toolbar.title = "Eventos"
                    replace(AdminEventsFragment())
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
}
