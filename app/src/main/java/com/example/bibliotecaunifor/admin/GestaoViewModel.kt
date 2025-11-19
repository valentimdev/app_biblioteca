package com.example.bibliotecaunifor.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.models.UserResponse
import com.example.bibliotecaunifor.utils.AuthUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class GestaoViewModel : ViewModel() {

    private val _todos = MutableStateFlow<List<User>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Lista filtrada
    val users: StateFlow<List<User>> =
        combine(_todos, _query) { list, q ->
            if (q.isBlank()) list
            else {
                val t = q.trim().lowercase()
                list.filter {
                    it.name.lowercase().contains(t) || it.matricula.contains(t)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Quantidade total
    val total: StateFlow<Int> =
        _todos.map { it.size }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Quantidade ativos
    val ativos: StateFlow<Int> =
        _todos.map { list -> list.count { it.status == UserStatus.ACTIVE } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Quantidade bloqueados
    val bloqueados: StateFlow<Int> =
        _todos.map { list -> list.count { it.status == UserStatus.BANNED } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // ============================
    // BUSCAR TODOS OS USUÁRIOS
    // ============================
    init {
        carregarUsuarios()
    }

    fun carregarUsuarios() {
        viewModelScope.launch {
            try {
                // Pega o token do AuthUtils (ou RetrofitClient se estiver setado lá)
                val tokenStr = RetrofitClient.getToken() ?: return@launch
                val token = "Bearer $tokenStr"

                // Passa token para o endpoint
                val response = RetrofitClient.userApi.getAllUsers(token).awaitResponse()

                if (response.isSuccessful) {
                    val lista = response.body()?.map { toUser(it) } ?: emptyList()
                    _todos.value = lista
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================
    // ALTERAR STATUS (BLOCK / UNBLOCK)
    // ============================
    // fun toggleStatus(userId: String) {
    //    viewModelScope.launch {
    //        try {
    //            val response = RetrofitClient.userApi.toggleStatus(userId).awaitResponse()

    //            if (response.isSuccessful) {
    //                carregarUsuarios()
    //            }

    //        } catch (e: Exception) {
    //            e.printStackTrace()
    //        }
    //    }
    // }

    fun setQuery(q: String) {
        viewModelScope.launch { _query.emit(q) }
    }

    // Converter UserResponse → User (model da UI)
    private fun toUser(r: UserResponse): User {
        return User(
            id = r.id,
            name = r.name,
            email = r.email,
            matricula = r.matricula,
            role = r.role,
            imageUrl = r.imageUrl,
            status = r.status ?: UserStatus.ACTIVE
        )
    }
}
