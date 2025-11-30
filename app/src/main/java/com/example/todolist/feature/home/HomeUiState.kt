package com.example.todolist.feature.home

import com.example.todolist.core.model.Task
import java.time.LocalDate
import java.time.YearMonth

/**
 * UI state for the Home screen. UI reads this and triggers events on the ViewModel.
 */
data class HomeUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

