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

    private lateinit var homeFragment: HomeFragment
    private lateinit var catalogFragment: CatalogUserFragment
    private lateinit var eventsFragment: EventsFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val savedToken = AuthUtils.getToken(this)
        RetrofitClient.setToken(savedToken)
        toolbar = findViewById(R.id.toolbar)
        bottom = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        homeFragment = HomeFragment()
        catalogFragment = CatalogUserFragment()
        eventsFragment = EventsFragment()
        chatFragment = ChatFragment()
        profileFragment = ProfileFragment()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .commit()
            bottom.selectedItemId = R.id.nav_home
            configureToolbarFor(homeFragment)
        }

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchTo(homeFragment, "home")
                R.id.nav_catalog -> switchTo(catalogFragment, "catalog")
                R.id.nav_events -> switchTo(eventsFragment, "events")
                R.id.nav_chat -> switchTo(chatFragment, "chat")
                R.id.nav_profile -> switchTo(profileFragment, "profile")
            }
            true
        }
    }

    private fun switchTo(fragment: Fragment, tag: String) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        val existing = fm.findFragmentByTag(tag)
        if (existing == null) {
            transaction.add(R.id.fragment_container, fragment, tag)
        }

        fm.fragments.forEach { f ->
            if (f.tag == tag) {
                transaction.show(f)
            } else {
                transaction.hide(f)
            }
        }

        transaction.commit()
        configureToolbarFor(fragment)
    }

    fun configureToolbarFor(f: Fragment) {
        toolbar.menu.clear()
        when (f) {
            is CatalogUserFragment -> toolbar.title = "CATÁLOGO"
            is EventsFragment -> toolbar.title = "EVENTOS"
            is ChatFragment -> {
                supportActionBar?.title = "Biblio Bot"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            is ProfileFragment -> toolbar.title = "PERFIL"
            else -> toolbar.title = "BibliotecaUnifor"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.navigationIcon = null
    }

    fun refreshHomeFragment() {
        val home = supportFragmentManager.findFragmentByTag("home") as? HomeFragment
        home?.reloadHome()
    }
}
