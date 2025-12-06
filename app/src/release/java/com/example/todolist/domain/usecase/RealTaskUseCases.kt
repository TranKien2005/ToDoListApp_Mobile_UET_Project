package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// Release implementations that use real TaskRepository
class RealGetTasksUseCase(
    private val repository: TaskRepository
) : GetTasksUseCase {
    override operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}

class RealCreateTaskUseCase(
    private val repository: TaskRepository
) : CreateTaskUseCase {
    override suspend operator fun invoke(task: Task) {
        repository.saveTask(task)
    }
}

class RealUpdateTaskUseCase(
    private val repository: TaskRepository
) : UpdateTaskUseCase {
    override suspend operator fun invoke(task: Task) {
        repository.saveTask(task)
    }
}

class RealDeleteTaskUseCase(
    private val repository: TaskRepository
) : DeleteTaskUseCase {
    override suspend operator fun invoke(taskId: Int) {
        repository.deleteTask(taskId)
    }
}

class RealGetTasksByDayUseCase(
    private val repository: TaskRepository
) : GetTasksByDayUseCase {
    override operator fun invoke(date: LocalDate): Flow<List<Task>> =
        repository.getTasks().map { list ->
            list.filter { task ->
                val startDate = task.startTime.toLocalDate()
                when (task.repeatType) {
                    RepeatType.NONE -> startDate.isEqual(date)
                    RepeatType.DAILY -> !date.isBefore(startDate)
                    RepeatType.WEEKLY -> !date.isBefore(startDate) && task.startTime.dayOfWeek == date.dayOfWeek
                    RepeatType.MONTHLY -> !date.isBefore(startDate) && task.startTime.dayOfMonth == date.dayOfMonth
                }
            }
        }
}

class RealGetTasksByMonthUseCase(
    private val repository: TaskRepository
) : GetTasksByMonthUseCase {
    override operator fun invoke(year: Int, month: Int): Flow<List<Task>> {
        val target = java.time.YearMonth.of(year, month)
        return repository.getTasks().map { list ->
            list.filter { task ->
                val startDate = task.startTime.toLocalDate()
                when (task.repeatType) {
                    RepeatType.NONE -> java.time.YearMonth.from(startDate) == target
                    RepeatType.DAILY -> !startDate.isAfter(target.atEndOfMonth())
                    RepeatType.WEEKLY -> {
                        val firstOfMonth = target.atDay(1)
                        val lastOfMonth = target.atEndOfMonth()
                        var candidate = firstOfMonth
                        val targetDow = task.startTime.dayOfWeek
                        while (candidate.dayOfWeek != targetDow && candidate <= lastOfMonth) {
                            candidate = candidate.plusDays(1)
                        }
                        !candidate.isAfter(lastOfMonth) && !candidate.isBefore(startDate)
                    }
                    RepeatType.MONTHLY -> {
                        val day = task.startTime.dayOfMonth
                        if (target.lengthOfMonth() < day) return@filter false
                        val occ = target.atDay(day)
                        !occ.isBefore(startDate)
                    }
                }
            }
        }
    }
}

