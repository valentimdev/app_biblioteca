package com.example.bibliotecaunifor

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class BookDetailFragment : Fragment(R.layout.fragment_book_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = requireArguments().getString("title")!!
        val author = requireArguments().getString("author")!!
        val id = requireArguments().getString("id")!!
        val cover = requireArguments().getInt("cover", 0)

        view.findViewById<TextView>(R.id.txtTitle).text = title
        view.findViewById<TextView>(R.id.txtAuthor).text = author
        view.findViewById<TextView>(R.id.txtId).text = "ID: $id"
        if (cover != 0) view.findViewById<ImageView>(R.id.imgCover).setImageResource(cover)
    }

    companion object {
        fun new(b: Book) = BookDetailFragment().apply {
            arguments = bundleOf(
                "id" to b.id,
                "title" to b.title,
                "author" to b.author,
                "cover" to (b.coverRes ?: 0)
            )
        }
    }
}
