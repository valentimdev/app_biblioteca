package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.adapters.BookAdapter
import com.example.bibliotecaunifor.CatalogFragment

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val sample = listOf(
        Book("1","Clean Code","Robert C. Martin"),
        Book("2","Kotlin in Action","D. Jemerov, S. Isakova"),
        Book("3","Effective Java","Joshua Bloch")
    )

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbar("CATALOGO", showBack = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBooks)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = BookAdapter(sample) { book ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment.new(book))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            // TODO: abrir dialog/fragment de filtros
        }

        val edt = view.findViewById<EditText>(R.id.edtSearch)
        edt.doAfterTextChanged { text ->
            // TODO: filtrar adapter por 'text'
        }
    }
}
