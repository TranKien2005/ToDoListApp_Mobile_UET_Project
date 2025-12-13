package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

// Debug-only fake implementations of task use-cases. These live in the `debug` source set
// so they will be compiled into debug builds and can be swapped for real implementations in release.

private val _tasksState = MutableStateFlow<List<Task>>(
    listOf(
        Task(id = 1, title = "Sample Task 1", description = "Fake task for debug...", startTime = LocalDateTime.now().withHour(9).withMinute(0), durationMinutes = 120, repeatType = RepeatType.NONE),
        Task(id = 2, title = "Daily Task", description = "Repeats every day", startTime = LocalDateTime.now().withHour(8).withMinute(0).minusDays(1), durationMinutes = 60, repeatType = RepeatType.DAILY),
        Task(id = 3, title = "Weekly Meeting", description = "Repeats weekly on the same weekday", startTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), durationMinutes = 90, repeatType = RepeatType.WEEKLY),
        Task(id = 4, title = "Monthly Report", description = "Repeats monthly on the same day", startTime = LocalDateTime.now().withHour(14).withMinute(30).withDayOfMonth(5), durationMinutes = 45, repeatType = RepeatType.MONTHLY),
        Task(id = 5, title = "One-off Later", description = null, startTime = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0), durationMinutes = null, repeatType = RepeatType.NONE),
        Task(id = 6, title = "No duration task", description = "Instant reminder", startTime = LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), durationMinutes = null, repeatType = RepeatType.NONE),
        Task(id = 7, title = "Past Completed", description = "Completed task", startTime = LocalDateTime.now().minusDays(1).withHour(11).withMinute(0), durationMinutes = 60, repeatType = RepeatType.NONE)
    )
)

class FakeGetTasksUseCase: GetTasksUseCase {
    override operator fun invoke(): Flow<List<Task>> = _tasksState
}

// New: separate create/update usecases. Create will auto-generate an id when given id == 0.
class FakeCreateTaskUseCase: CreateTaskUseCase {
    override suspend operator fun invoke(task: Task) {
        val current = _tasksState.value.toMutableList()
        // generate id = maxId + 1
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val created = task.copy(id = nextId)
        current.add(created)
        _tasksState.value = current
    }
}

class FakeUpdateTaskUseCase: UpdateTaskUseCase {
    override suspend operator fun invoke(task: Task) {
        val current = _tasksState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == task.id }
        if (idx >= 0) current[idx] = task
        _tasksState.value = current
    }
}

class FakeDeleteTaskUseCase: DeleteTaskUseCase {
    override suspend operator fun invoke(taskId: Int) {
        _tasksState.value = _tasksState.value.filterNot { it.id == taskId }
    }
}

// New debug fakes for day/month filtering that take repeats into account
class FakeGetTasksByDayUseCase: GetTasksByDayUseCase {
    override operator fun invoke(date: LocalDate): Flow<List<Task>> =
        _tasksState.map { list ->
            list.filter { task ->
                val startDate = task.startTime.toLocalDate()
                // Only consider occurrences on/after start date
                when (task.repeatType) {
                    RepeatType.NONE -> startDate.isEqual(date)
                    RepeatType.DAILY -> !date.isBefore(startDate)
                    RepeatType.WEEKLY -> !date.isBefore(startDate) && task.startTime.dayOfWeek == date.dayOfWeek
                    RepeatType.MONTHLY -> !date.isBefore(startDate) && task.startTime.dayOfMonth == date.dayOfMonth
                }
            }
        }
}

class FakeGetTasksByMonthUseCase: GetTasksByMonthUseCase {
    override operator fun invoke(year: Int, month: Int): Flow<List<Task>> {
        val target = java.time.YearMonth.of(year, month)
        return _tasksState.map { list ->
            list.filter { task ->
                val startDate = task.startTime.toLocalDate()
                when (task.repeatType) {
                    RepeatType.NONE -> java.time.YearMonth.from(startDate) == target
                    RepeatType.DAILY -> !startDate.isAfter(target.atEndOfMonth())
                    RepeatType.WEEKLY -> {
                        // is there any day in the target month with the same dayOfWeek and on/after startDate
                        val firstOfMonth = target.atDay(1)
                        val lastOfMonth = target.atEndOfMonth()
                        // find the first date in month that matches weekday
                        var candidate = firstOfMonth
                        // advance to matching weekday
                        val targetDow = task.startTime.dayOfWeek
                        while (candidate.dayOfWeek != targetDow && candidate <= lastOfMonth) {
                            candidate = candidate.plusDays(1)
                        }
                        !candidate.isAfter(lastOfMonth) && !candidate.isBefore(startDate)
                    }
                    RepeatType.MONTHLY -> {
                        val day = task.startTime.dayOfMonth
                        // if this month has that day and it's on/after startDate
                        if (target.lengthOfMonth() < day) return@filter false
                        val occ = target.atDay(day)
                        !occ.isBefore(startDate)
                    }
                }
            }
        }
    }
}

val fakeTaskUseCases = TaskUseCases(
    getTasks = FakeGetTasksUseCase(),
    createTask = FakeCreateTaskUseCase(),
    updateTask = FakeUpdateTaskUseCase(),
    deleteTask = FakeDeleteTaskUseCase(),
    getTasksByDay = FakeGetTasksByDayUseCase(),
    getTasksByMonth = FakeGetTasksByMonthUseCase()
)

// Fake TaskRepository for Calendar Sync
val fakeTaskRepository = object : com.example.todolist.domain.repository.TaskRepository {
    override fun getTasks(): Flow<List<Task>> = _tasksState
    
    override suspend fun saveTask(task: Task) {
        val current = _tasksState.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.id == task.id }
        if (existingIdx >= 0) {
            current[existingIdx] = task
        } else {
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current.add(task.copy(id = nextId))
        }
        _tasksState.value = current
    }
    
    override suspend fun deleteTask(taskId: Int) {
        _tasksState.value = _tasksState.value.filterNot { it.id == taskId }
    }
}
