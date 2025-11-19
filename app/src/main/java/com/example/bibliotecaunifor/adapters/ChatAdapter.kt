// app/src/main/java/com/example/bibliotecaunifor/adapters/ChatAdapter.kt
package com.example.bibliotecaunifor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.databinding.ItemMessageReceivedBinding
import com.example.bibliotecaunifor.databinding.ItemMessageSentBinding
import com.example.bibliotecaunifor.models.Mensagem

class ChatAdapter(private val messages: List<Mensagem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val VIEW_TYPE_TYPING = 3

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isTyping -> VIEW_TYPE_TYPING
            message.isSentByUser -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_TYPING -> {
                val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TypingViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message.text)
            is ReceivedMessageViewHolder -> holder.bind(message.text)
            is TypingViewHolder -> holder.bind()
        }
    }

    override fun getItemCount() = messages.size

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.textMessageSent.text = text
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.textMessageReceived.text = text
        }
    }

    class TypingViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.textMessageReceived.text = "Biblio Bot está digitando..."
            binding.textMessageReceived.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }
}