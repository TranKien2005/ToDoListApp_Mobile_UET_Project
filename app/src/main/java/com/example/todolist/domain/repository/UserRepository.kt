package com.example.todolist.domain.repository

import com.example.todolist.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser()
}

