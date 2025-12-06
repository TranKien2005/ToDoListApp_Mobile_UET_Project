package com.example.todolist.feature.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.User
import com.example.todolist.domain.usecase.UserUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCases: UserUseCases
) : ViewModel() {

    val user: StateFlow<User?> = userUseCases.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveUser(user: User) {
        viewModelScope.launch {
            userUseCases.saveUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userUseCases.updateUser(user)
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            userUseCases.deleteUser()
        }
    }
}

