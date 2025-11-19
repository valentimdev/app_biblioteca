package com.example.bibliotecaunifor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotecaunifor.MainActivity
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.adapters.ChatAdapter
import com.example.bibliotecaunifor.api.ChatService
import com.example.bibliotecaunifor.databinding.FragmentChatBinding
import com.example.bibliotecaunifor.models.Mensagem
import com.example.bibliotecaunifor.utils.AuthUtils
import kotlinx.coroutines.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Mensagem>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LOG CORRETO — só roda DEPOIS que a view foi medida
        view.post {
            android.util.Log.d("ChatFragment", "View width: ${view.width}, height: ${view.height}")
            android.util.Log.d("ChatFragment", "RecyclerView height: ${binding.recyclerViewChat.height}")
        }

        setupRecyclerView()
        setupSendButton()
        sendWelcomeMessage()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)

        // GARANTE que o RecyclerView já existe antes de colocar o adapter
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            setHasFixedSize(true)  // melhora performance
            adapter = this@ChatFragment.adapter  // forma correta e segura
        }

        // Scroll automático PERFEITO (sem erro de null)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
            }
        })
    }

    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addMessage(text, true)
            binding.editTextMessage.text?.clear()

            addTypingIndicator()

            scope.launch {
                try {
                    val token = AuthUtils.getToken(requireContext())   // ← MUDOU AQUI
                    if (token.isNullOrEmpty()) {
                        replaceTypingWith("Erro: faça login novamente.")
                        return@launch
                    }

                    val resposta = withContext(Dispatchers.IO) {
                        ChatService.sendMessage(token, text)   // agora vai com o token certo!
                    }

                    replaceTypingWith(resposta)

                } catch (e: Exception) {
                    replaceTypingWith("Erro de conexão. Tente novamente.")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendWelcomeMessage() {
        addMessage("Oi! Sou o Biblio Bot, assistente da Biblioteca da UNIFOR.\nComo posso ajudar?", false)
    }

    private fun addMessage(text: String, isSentByUser: Boolean, isTyping: Boolean = false) {
        messages.add(Mensagem(text, isSentByUser, isTyping))
        adapter.notifyItemInserted(messages.size - 1)
    }

    private fun addTypingIndicator() {
        addMessage("Biblio Bot está digitando...", false, true)
    }

    private fun replaceTypingWith(text: String) {
        val lastIndex = messages.size - 1
        if (lastIndex >= 0 && messages[lastIndex].isTyping) {
            messages.removeAt(lastIndex)
            adapter.notifyItemRemoved(lastIndex)
        }
        addMessage(text, false)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).configureToolbarFor(this)
        // Esconde o título da toolbar e coloca só "Chat" ou "Biblio Bot"
        (requireActivity() as MainActivity).supportActionBar?.title = "Biblio Bot"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}