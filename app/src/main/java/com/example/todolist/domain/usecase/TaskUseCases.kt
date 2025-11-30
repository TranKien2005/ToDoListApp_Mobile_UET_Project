package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Keep only interface definitions in `main`. Concrete implementations must live in debug/release.
interface GetTasksUseCase {
    operator fun invoke(): Flow<List<Task>>
}

interface SaveTaskUseCase {
    suspend operator fun invoke(task: Task)
}

interface DeleteTaskUseCase {
    suspend operator fun invoke(taskId: Int)
}

interface MarkCompletedUseCase {
    suspend operator fun invoke(taskId: Int, completed: Boolean)
}

// Interfaces for day/month queries (no implementations in main)
interface GetTasksByDayUseCase {
    operator fun invoke(date: LocalDate): Flow<List<Task>>
}

interface GetTasksByMonthUseCase {
    operator fun invoke(year: Int, month: Int): Flow<List<Task>>
}

// Aggregator contains only the interfaces; implementations come from debug/release DI.
data class TaskUseCases(
    val getTasks: GetTasksUseCase,
    val saveTask: SaveTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val markCompleted: MarkCompletedUseCase,
    val getTasksByDay: GetTasksByDayUseCase,
    val getTasksByMonth: GetTasksByMonthUseCase
)
