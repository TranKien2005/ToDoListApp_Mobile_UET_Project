package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskDao
import com.example.todolist.data.mapper.TaskEntityMapper
import com.example.todolist.core.model.Task
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {
    override fun getTasks(): Flow<List<Task>> = dao.getAll().map { list -> list.map { TaskEntityMapper.toDomain(it) } }

    override suspend fun saveTask(task: Task) {
        val entity = TaskEntityMapper.fromDomain(task)
        dao.insert(entity)
    }

    override suspend fun deleteTask(taskId: Int) {
        dao.deleteById(taskId)
    }

    override suspend fun markCompleted(taskId: Int, completed: Boolean) {
        dao.updateCompleted(taskId, completed)
    }
}
