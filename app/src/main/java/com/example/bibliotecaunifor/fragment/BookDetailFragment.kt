package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class BookDetailFragment : Fragment(R.layout.fragment_book_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nome = requireArguments().getString("nome")!!
        val autor = requireArguments().getString("autor")!!
        val id = requireArguments().getString("id")!!
        val cover = requireArguments().getInt("cover", 0)

        view.findViewById<TextView>(R.id.txtTitle).text = nome
        view.findViewById<TextView>(R.id.txtAuthor).text = autor
        view.findViewById<TextView>(R.id.txtId).text = "ID: $id"
        if (cover != 0) {
            view.findViewById<ImageView>(R.id.imgCover).setImageResource(cover)
        }
    }

    companion object {
        fun new(b: Book) = BookDetailFragment().apply {
            arguments = bundleOf(
                "id" to b.id,
                "nome" to b.nome,
                "autor" to b.autor,
                "cover" to 0
            )
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
    }
}
