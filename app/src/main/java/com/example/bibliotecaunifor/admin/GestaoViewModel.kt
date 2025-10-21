package com.example.bibliotecaunifor.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GestaoViewModel : ViewModel() {

    private val _todos = MutableStateFlow<List<User>>(mock())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val users: StateFlow<List<User>> =
        combine(_todos, _query) { list, q ->
            if (q.isBlank()) list
            else {
                val t = q.trim().lowercase()
                list.filter {
                    it.nome.lowercase().contains(t) || it.matricula.contains(t)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val total: StateFlow<Int> =
        _todos.map { it.size }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val ativos: StateFlow<Int> =
        _todos.map { it.count { u -> u.status == UserStatus.ATIVO } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val bloqueados: StateFlow<Int> =
        _todos.map { it.count { u -> u.status == UserStatus.BLOQUEADO } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setQuery(q: String) {
        viewModelScope.launch { _query.emit(q) }
    }

    fun alternarStatus(id: String) {
        val nova = _todos.value.map {
            if (it.id == id) {
                it.copy(status = if (it.status == UserStatus.ATIVO) UserStatus.BLOQUEADO else UserStatus.ATIVO)
            } else it
        }
        _todos.value = nova
    }

    private fun mock(): List<User> = listOf(
        User("1", "Zezinho Sicrano", "20230001", UserStatus.ATIVO),
        User("2", "Zezinho Fulano",  "20230002", UserStatus.ATIVO),
        User("3", "Zezinho Beltrano","20230003", UserStatus.BLOQUEADO),
        User("4", "Maria Souza",     "20231234", UserStatus.ATIVO),
        User("5", "João Lima",       "20231235", UserStatus.BLOQUEADO)
    )
}