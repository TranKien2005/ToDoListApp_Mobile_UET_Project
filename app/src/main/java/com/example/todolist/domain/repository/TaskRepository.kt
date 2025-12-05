package com.example.todolist.domain.repository

import com.example.todolist.core.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun saveTask(task: Task)
    suspend fun deleteTask(taskId: Int)
    // Tasks are schedules only; no completion state
}
