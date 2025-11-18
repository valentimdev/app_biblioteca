package com.example.bibliotecaunifor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.fragment.ChatFragment
import com.example.bibliotecaunifor.fragment.EventsFragment
import com.example.bibliotecaunifor.fragment.ProfileFragment
import com.example.bibliotecaunifor.fragment.HomeFragment
import com.example.bibliotecaunifor.CatalogUserFragment
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottom: BottomNavigationView

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val savedToken = AuthUtils.getToken(this)
        RetrofitClient.setToken(savedToken)
        toolbar = findViewById(R.id.toolbar)
        bottom = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // tela inicial (Home)
        if (savedInstanceState == null) {
            switchTo(HomeFragment(), "home")
            bottom.selectedItemId = R.id.nav_home
        }

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchTo(HomeFragment(), "home")
                R.id.nav_catalog -> switchTo(CatalogUserFragment(), "catalog")
                R.id.nav_events -> switchTo(EventsFragment(), "events")
                R.id.nav_chat -> switchTo(ChatFragment(), "chat")
                R.id.nav_profile -> switchTo(ProfileFragment(), "profile")
            }
            true
        }
    }

    private fun switchTo(fragment: Fragment, tag: String) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
        configureToolbarFor(fragment)
    }

    fun configureToolbarFor(f: Fragment) {
        toolbar.menu.clear()
        when (f) {
            is CatalogUserFragment -> toolbar.title = "CATÁLOGO"
            is EventsFragment -> toolbar.title = "EVENTOS"
            is ChatFragment -> toolbar.title = "CHAT"
            is ProfileFragment -> toolbar.title = "PERFIL"
            else -> toolbar.title = "BibliotecaUnifor"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.navigationIcon = null
    }

    // FUNÇÃO QUE ATUALIZA A HOME
    fun refreshHomeFragment() {
        val homeFragment = supportFragmentManager.findFragmentByTag("home") as? HomeFragment
        homeFragment?.reloadHome()
    }
}