@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.todolist.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.usecase.TaskUseCases
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.combine

/**
 * Home ViewModel in `main` source set. It receives TaskUseCases via constructor so DI
 * can provide debug or release implementations. Do not reference debug-only classes here.
 */
class HomeViewModel(
    private val taskUseCases: TaskUseCases,
    initialDate: LocalDate = LocalDate.now()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(currentMonth = YearMonth.from(initialDate), selectedDate = initialDate))
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _selectedDate = MutableStateFlow(initialDate)
    // A small integer trigger that we increment to force re-collection even when selectedDate is unchanged
    private val _refreshTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            // Combine selected date with refresh trigger so that refresh() (incrementing trigger)
            // forces flatMapLatest to re-subscribe to the getTasksByDay flow.
            combine(_selectedDate, _refreshTrigger) { date, _ -> date }
                .flatMapLatest { date -> taskUseCases.getTasksByDay(date) }
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks, isLoading = false, error = null) }
                }
        }

        // initial load
        refresh()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date, currentMonth = YearMonth.from(date)) }
    }

    fun prevMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        val newSelected = LocalDate.of(newMonth.year, newMonth.monthValue, 1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = newSelected) }
        _selectedDate.value = newSelected
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        val newSelected = LocalDate.of(newMonth.year, newMonth.monthValue, 1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = newSelected) }
        _selectedDate.value = newSelected
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        // increment trigger to force re-collection even if selectedDate is unchanged
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    // New: delete a task by id and refresh the list
    fun deleteTask(taskId: Int) {
        // optimistic UI update: remove locally first so the user sees immediate feedback
        _uiState.update { current -> current.copy(tasks = current.tasks.filterNot { it.id == taskId }) }

        viewModelScope.launch {
            try {
                taskUseCases.deleteTask.invoke(taskId)
                // ensure we have fresh data from source
                refresh()
            } catch (t: Throwable) {
                // On error, set error message and trigger a refresh to reload from source
                _uiState.update { it.copy(isLoading = false, error = t.message) }
                refresh()
            }
        }
    }

    // New: mark a task completed/uncompleted and refresh
    fun markTaskCompleted(taskId: Int, completed: Boolean = true) {
        // optimistic update locally
        _uiState.update { current -> current.copy(tasks = current.tasks.map { if (it.id == taskId) it.copy(isCompleted = completed) else it }) }

        viewModelScope.launch {
            try {
                taskUseCases.markCompleted.invoke(taskId, completed)
                refresh()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = t.message) }
                refresh()
            }
        }
    }
}
