package com.example.bibliotecaunifor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliotecaunifor.databinding.ActivityMainBinding
import com.example.bibliotecaunifor.CatalogFragment
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        if (savedInstanceState == null) {
            replace(HomeFragment())
        }

        vb.bottomNavigation.setOnItemSelectedListener(navListener)
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> replace(HomeFragment())
            R.id.nav_catalog -> replace(CatalogFragment())
            R.id.nav_events -> replace(EventsFragment())
            R.id.nav_chat -> replace(ChatFragment())
            R.id.nav_profile -> replace(ProfileFragment())
            else -> false
        }
        true
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
