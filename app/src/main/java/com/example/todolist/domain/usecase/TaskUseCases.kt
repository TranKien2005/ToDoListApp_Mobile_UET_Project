package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Keep only interface definitions in `main`. Concrete implementations must live in debug/release.
interface GetTasksUseCase {
    operator fun invoke(): Flow<List<Task>>
}

// Split save into create/update to avoid repository-id generation being performed in ViewModel.
interface CreateTaskUseCase {
    suspend operator fun invoke(task: Task)
}

interface UpdateTaskUseCase {
    suspend operator fun invoke(task: Task)
}

interface DeleteTaskUseCase {
    suspend operator fun invoke(taskId: Int)
}

// Interfaces for day/month queries (no implementations in main)
interface GetTasksByDayUseCase {
    operator fun invoke(date: LocalDate): Flow<List<Task>>
}

interface GetTasksByMonthUseCase {
    operator fun invoke(year: Int, month: Int): Flow<List<Task>>
}

// Aggregator contains only the interfaces; implementations come from debug/release.
data class TaskUseCases(
    val getTasks: GetTasksUseCase,
    val createTask: CreateTaskUseCase,
    val updateTask: UpdateTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val getTasksByDay: GetTasksByDayUseCase,
    val getTasksByMonth: GetTasksByMonthUseCase
)
