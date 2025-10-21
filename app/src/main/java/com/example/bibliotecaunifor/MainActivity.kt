package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import br.unifor.biblioteca.admin.GestaoFragment

import com.example.bibliotecaunifor.fragment.ChatFragment
import com.example.bibliotecaunifor.fragment.EventsFragment
import com.example.bibliotecaunifor.fragment.ProfileFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottom: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottom = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Clique no sino (vale pra qualquer tela que inflar o menu com esse item)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    // Troque para sua activity real ou abra um fragment
                    // startActivity(Intent(this, NotificacoesActivity::class.java))
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            switchTo(HomeFragment())
            bottom.selectedItemId = R.id.nav_home
        }

        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home    -> switchTo(HomeFragment())
                R.id.nav_catalog -> switchTo(CatalogFragment())
                R.id.nav_events  -> switchTo(EventsFragment())
                R.id.nav_gestao  -> switchTo(GestaoFragment())
                R.id.nav_chat    -> switchTo(ChatFragment())
                R.id.nav_profile -> switchTo(ProfileFragment())
            }
            true
        }
    }

    private fun switchTo(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit()
        configureToolbarFor(f)
    }

    fun configureToolbarFor(f: Fragment) {
        toolbar.menu.clear()
        when (f) {
            is GestaoFragment -> {
                toolbar.title = "GESTÃO"
                toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                toolbar.inflateMenu(R.menu.top_app_bar) // exibe o sino
                toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            }
            is CatalogFragment -> {
                toolbar.title = "CATÁLOGO"
                clearBackAndMenu()
            }
            is EventsFragment -> {
                toolbar.title = "EVENTOS"
                clearBackAndMenu()
            }
            is ChatFragment -> {
                toolbar.title = "CHAT"
                clearBackAndMenu()
            }
            is ProfileFragment -> {
                toolbar.title = "PERFIL"
                clearBackAndMenu()
            }
            else -> {
                toolbar.title = "BibliotecaUnifor"
                clearBackAndMenu()
            }
        }
    }

    private fun clearBackAndMenu() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.navigationIcon = null
        // menu já foi limpo em toolbar.menu.clear()
    }
}




