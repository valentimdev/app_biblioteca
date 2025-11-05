package com.example.bibliotecaunifor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.fragment.ChatFragment
import com.example.bibliotecaunifor.fragment.EventsFragment
import com.example.bibliotecaunifor.fragment.ProfileFragment
import com.example.bibliotecaunifor.CatalogUserFragment
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

        // tela inicial (Home)
        if (savedInstanceState == null) {
            switchTo(HomeFragment())
            bottom.selectedItemId = R.id.nav_home
        }

        // navegação inferior
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> switchTo(HomeFragment())
                R.id.nav_catalog -> switchTo(CatalogUserFragment()) // catálogo do usuário
                R.id.nav_events -> switchTo(EventsFragment())
                R.id.nav_chat -> switchTo(ChatFragment())
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
            is CatalogUserFragment -> {
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
                toolbar.navigationIcon = null
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    private fun clearBackAndMenu() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.navigationIcon = null
        toolbar.menu.clear()
    }
}
