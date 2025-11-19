package com.example.bibliotecaunifor.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliotecaunifor.api.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GestaoViewModel : ViewModel() {

    private val _todos = MutableStateFlow<List<User>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

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

    val total: StateFlow<Int> =
        _todos.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setQuery(q: String) {
        viewModelScope.launch { _query.emit(q) }
    }

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val token = "Bearer ${RetrofitClient.getToken()}" // ou AuthUtils.getToken(context)
                val response = RetrofitClient.userApi.getAllUsers(token).execute()
                if (response.isSuccessful) {
                    _todos.value = response.body()?.map {
                        User(
                            id = it.id,
                            name = it.name,
                            email = it.email,
                            matricula = it.matricula,
                            role = it.role,
                            imageUrl = it.imageUrl
                        )
                    } ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
