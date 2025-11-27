package com.example.todolist.data.local

import com.example.todolist.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Simple fake DAO used by unit tests if needed
class FakeTaskDao {
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())
    fun getAll(): Flow<List<TaskEntity>> = flow
    suspend fun insert(entity: TaskEntity) { flow.value = flow.value + entity }
}

