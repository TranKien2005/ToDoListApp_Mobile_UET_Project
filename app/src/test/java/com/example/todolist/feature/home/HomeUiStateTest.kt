package com.example.todolist.feature.home

import com.example.todolist.core.model.Task
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Unit tests for HomeUiState
 */
class HomeUiStateTest {

    @Test
    fun `default state has correct values`() {
        val state = HomeUiState()

        assertEquals(LocalDate.now(), state.selectedDate)
        assertEquals(YearMonth.now(), state.currentMonth)
        assertTrue(state.tasks.isEmpty())
        // Actual default is false
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `state can be updated with tasks`() {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", startTime = LocalDateTime.now()),
            Task(id = 2, title = "Task 2", startTime = LocalDateTime.now().plusHours(1))
        )

        val state = HomeUiState(
            selectedDate = LocalDate.now(),
            tasks = tasks,
            isLoading = false
        )

        assertEquals(2, state.tasks.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `state with error`() {
        val state = HomeUiState(
            error = "Failed to load tasks",
            isLoading = false
        )

        assertEquals("Failed to load tasks", state.error)
    }

    @Test
    fun `state copy works correctly`() {
        val original = HomeUiState()
        val modified = original.copy(selectedDate = LocalDate.now().plusDays(1))

        assertEquals(LocalDate.now(), original.selectedDate)
        assertEquals(LocalDate.now().plusDays(1), modified.selectedDate)
    }
}
