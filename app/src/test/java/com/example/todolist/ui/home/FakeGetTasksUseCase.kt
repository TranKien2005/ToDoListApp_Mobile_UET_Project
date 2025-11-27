package com.example.todolist.ui.home

import com.example.todolist.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

class FakeGetTasksUseCase {
    operator fun invoke(): Flow<List<Task>> {
        val sample = Task(id = 1, title = "Sample", description = null, startTime = LocalDateTime.now(), endTime = null)
        return flowOf(listOf(sample))
    }
}

