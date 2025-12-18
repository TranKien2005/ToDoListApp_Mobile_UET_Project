package com.example.todolist.feature.analysis

import com.example.todolist.domain.usecase.StatsGranularity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for AnalysisUiState
 */
class AnalysisUiStateTest {

    @Test
    fun `default state has correct values`() {
        val state = AnalysisUiState()

        assertEquals(LocalDate.now(), state.referenceDate)
        // Actual default is WEEK_OF_MONTH
        assertEquals(StatsGranularity.WEEK_OF_MONTH, state.granularity)
        assertTrue(state.isLoading)
        assertTrue(state.stats.isEmpty())
    }

    @Test
    fun `state can be updated with loading false`() {
        val state = AnalysisUiState(
            referenceDate = LocalDate.now(),
            granularity = StatsGranularity.DAY_OF_WEEK,
            isLoading = false
        )

        assertFalse(state.isLoading)
        assertEquals(StatsGranularity.DAY_OF_WEEK, state.granularity)
    }

    @Test
    fun `stats granularity enum values`() {
        val granularities = StatsGranularity.values()
        assertTrue(granularities.contains(StatsGranularity.DAY_OF_WEEK))
        assertTrue(granularities.contains(StatsGranularity.WEEK_OF_MONTH))
        assertTrue(granularities.contains(StatsGranularity.MONTH_OF_YEAR))
    }

    @Test
    fun `state copy works correctly`() {
        val original = AnalysisUiState()
        val modified = original.copy(granularity = StatsGranularity.MONTH_OF_YEAR)

        // Original keeps WEEK_OF_MONTH (default)
        assertEquals(StatsGranularity.WEEK_OF_MONTH, original.granularity)
        assertEquals(StatsGranularity.MONTH_OF_YEAR, modified.granularity)
    }
}
