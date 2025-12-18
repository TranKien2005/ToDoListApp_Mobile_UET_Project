package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for MissionUseCases related types
 */
class MissionUseCasesTest {

    @Test
    fun `stats granularity enum values`() {
        val values = StatsGranularity.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(StatsGranularity.DAY))
        assertTrue(values.contains(StatsGranularity.DAY_OF_WEEK))
        assertTrue(values.contains(StatsGranularity.WEEK_OF_MONTH))
        assertTrue(values.contains(StatsGranularity.MONTH_OF_YEAR))
    }

    @Test
    fun `mission stats entry creation`() {
        val entry = MissionStatsEntry(
            label = "Mon",
            startDate = LocalDate.now(),
            completed = 5,
            missed = 2,
            inProgress = 3
        )

        assertEquals("Mon", entry.label)
        assertEquals(5, entry.completed)
        assertEquals(2, entry.missed)
        assertEquals(3, entry.inProgress)
    }

    @Test
    fun `mission stats entry total calculation`() {
        val entry = MissionStatsEntry(
            label = "Day",
            startDate = LocalDate.now(),
            completed = 5,
            missed = 2,
            inProgress = 3
        )

        val total = entry.completed + entry.missed + entry.inProgress
        assertEquals(10, total)
    }

    @Test
    fun `mission stats entry equality`() {
        val date = LocalDate.now()
        val entry1 = MissionStatsEntry("Mon", date, 1, 2, 3)
        val entry2 = MissionStatsEntry("Mon", date, 1, 2, 3)
        
        assertEquals(entry1, entry2)
    }

    @Test
    fun `mission stats entry for empty day`() {
        val entry = MissionStatsEntry(
            label = "Empty",
            startDate = LocalDate.now(),
            completed = 0,
            missed = 0,
            inProgress = 0
        )

        assertEquals(0, entry.completed)
        assertEquals(0, entry.missed)
        assertEquals(0, entry.inProgress)
    }

    @Test
    fun `stats granularity ordinals`() {
        assertEquals(0, StatsGranularity.DAY.ordinal)
        assertEquals(1, StatsGranularity.DAY_OF_WEEK.ordinal)
        assertEquals(2, StatsGranularity.WEEK_OF_MONTH.ordinal)
        assertEquals(3, StatsGranularity.MONTH_OF_YEAR.ordinal)
    }
}
