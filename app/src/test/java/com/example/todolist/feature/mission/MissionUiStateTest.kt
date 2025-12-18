package com.example.todolist.feature.mission

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.domain.usecase.StatsGranularity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for MissionUiState
 */
class MissionUiStateTest {

    @Test
    fun `default state has correct values`() {
        val state = MissionUiState()

        assertEquals(LocalDate.now(), state.referenceDate)
        assertEquals(StatsGranularity.DAY_OF_WEEK, state.granularity)
        assertTrue(state.missions.isEmpty())
        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `state can be updated with missions`() {
        val missions = listOf(
            Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.now().plusDays(1)),
            Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.now().plusDays(7))
        )

        val state = MissionUiState(
            missions = missions,
            isLoading = false
        )

        assertEquals(2, state.missions.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `state with error`() {
        val state = MissionUiState(
            error = "Failed to load missions",
            isLoading = false
        )

        assertEquals("Failed to load missions", state.error)
    }

    @Test
    fun `granularity can be changed`() {
        val original = MissionUiState()
        val modified = original.copy(granularity = StatsGranularity.MONTH_OF_YEAR)

        assertEquals(StatsGranularity.DAY_OF_WEEK, original.granularity)
        assertEquals(StatsGranularity.MONTH_OF_YEAR, modified.granularity)
    }

    @Test
    fun `reference date can be changed`() {
        val original = MissionUiState()
        val newDate = LocalDate.now().plusWeeks(1)
        val modified = original.copy(referenceDate = newDate)

        assertEquals(newDate, modified.referenceDate)
    }
}
