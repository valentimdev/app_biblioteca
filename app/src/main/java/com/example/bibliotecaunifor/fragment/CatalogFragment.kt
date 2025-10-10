package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.CatalogFragment

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val sample = listOf(
        Book("1","Clean Code","Robert C. Martin"),
        Book("2","Kotlin in Action","Dmitry Jemerov, Svetlana Isakova"),
        Book("3","Effective Java","Joshua Bloch")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = BookAdapter(sample) { book ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment.new(book))
                .addToBackStack(null)
                .commit()
        }
    }
}
