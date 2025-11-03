package com.example.bibliotecaunifor

data class AdminEvento(
    val nome: String,
    val local: String,
    val vagas: Int,
    val data: String,         // "dd/MM/yyyy"
    val horario: String,      // "HH:mm"
    val ativo: Boolean = true,
    val inscricaoAtiva: Boolean = true
) {
    val isEncerrado: Boolean
        get() {
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
            val eventoTime = formatter.parse("$data $horario") ?: return false
            return eventoTime.before(java.util.Date())
        }

    val isAberto: Boolean
        get() = !isEncerrado
}
