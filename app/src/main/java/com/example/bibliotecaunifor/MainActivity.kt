package com.example.bibliotecaunifor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.fragment.*
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

    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val savedToken = AuthUtils.getToken(this)
        RetrofitClient.setToken(savedToken)

        toolbar = findViewById(R.id.toolbar)
        bottom = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Instancia os fragments
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

        // Navegação principal
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchTo(homeFragment, "home")
                R.id.nav_catalog -> switchTo(catalogFragment, "catalog")
                R.id.nav_events -> switchTo(eventsFragment, "events")
                R.id.nav_chat -> switchTo(chatFragment, "chat")
                R.id.nav_profile -> {
                    switchTo(profileFragment, "profile")
                    (profileFragment as? ProfileFragment)?.carregarDadosDoUsuario()
                }
            }
            true
        }

        // Re-selecionar mesma aba
        bottom.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    val profile = supportFragmentManager.findFragmentByTag("profile") as? ProfileFragment
                    profile?.carregarDadosDoUsuario()
                }
                R.id.nav_home -> {
                    val home = supportFragmentManager.findFragmentByTag("home") as? HomeFragment
                    home?.reload()
                }
            }
        }

        // 🔔 pedir permissão de notificação (Android 13+)
        pedirPermissaoNotificacoesSePrecisar()
    }

    private fun pedirPermissaoNotificacoesSePrecisar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            // Se quiser, você pode tratar aceito / negado aqui
            // por enquanto não é obrigatório fazer nada
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
            } else if (f.isAdded) {
                transaction.hide(f)
            }
        }

        transaction.commitNow()
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
        home?.reload()
    }
}
