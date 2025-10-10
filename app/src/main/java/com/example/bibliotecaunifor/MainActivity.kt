package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.databinding.ActivityMainBinding
import com.example.bibliotecaunifor.CatalogFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    lateinit var bottom: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottom = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.title = "CATALOGO" // título inicial, mude se quiser "Início"

        // menu do sino
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            replace(CatalogFragment())
            bottom.selectedItemId = R.id.nav_catalog
        }

        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home    -> replace(HomeFragment())
                R.id.nav_catalog -> replace(CatalogFragment())
                R.id.nav_events  -> replace(EventsFragment())
                R.id.nav_chat    -> replace(ChatFragment())
                R.id.nav_profile -> replace(ProfileFragment())
            }
            true
        }
    }

    fun setToolbar(title: String, showBack: Boolean) {
        toolbar.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        toolbar.navigationIcon = if (showBack) AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24) else null
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun replace(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit()
    }
}



class HomeFragment : Fragment(android.R.layout.simple_list_item_1)
class EventsFragment : Fragment(android.R.layout.simple_list_item_1)
class ChatFragment : Fragment(android.R.layout.simple_list_item_1)
class ProfileFragment : Fragment(android.R.layout.simple_list_item_1)
