    package com.example.bibliotecaunifor.adapters

    import android.view.LayoutInflater
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.example.bibliotecaunifor.R

    class LivroAdapter(
        private val itens: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

        inner class LivroViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val image: ImageView = itemView.findViewById(R.id.imageItem)
            val text: TextView = itemView.findViewById(R.id.textItemTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_carrossel, parent, false)
            return LivroViewHolder(view)
        }

        override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
            val titulo = itens[position]
            holder.text.text = titulo
            holder.image.setImageResource(R.drawable.ic_launcher_foreground)
            holder.itemView.setOnClickListener { onClick(titulo) }
        }

        override fun getItemCount(): Int = itens.size
    }