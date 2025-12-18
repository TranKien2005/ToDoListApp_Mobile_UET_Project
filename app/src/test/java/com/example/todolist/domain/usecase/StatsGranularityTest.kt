package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StatsGranularity enum
 */
class StatsGranularityTest {

    @Test
    fun `all granularity values exist`() {
        assertEquals(4, StatsGranularity.values().size)
        assertTrue(StatsGranularity.values().contains(StatsGranularity.DAY))
        assertTrue(StatsGranularity.values().contains(StatsGranularity.DAY_OF_WEEK))
        assertTrue(StatsGranularity.values().contains(StatsGranularity.WEEK_OF_MONTH))
        assertTrue(StatsGranularity.values().contains(StatsGranularity.MONTH_OF_YEAR))
    }

    @Test
    fun `granularity values can be retrieved by name`() {
        assertEquals(StatsGranularity.DAY, StatsGranularity.valueOf("DAY"))
        assertEquals(StatsGranularity.DAY_OF_WEEK, StatsGranularity.valueOf("DAY_OF_WEEK"))
        assertEquals(StatsGranularity.WEEK_OF_MONTH, StatsGranularity.valueOf("WEEK_OF_MONTH"))
        assertEquals(StatsGranularity.MONTH_OF_YEAR, StatsGranularity.valueOf("MONTH_OF_YEAR"))
    }

    @Test
    fun `granularity ordinal values`() {
        assertEquals(0, StatsGranularity.DAY.ordinal)
        assertEquals(1, StatsGranularity.DAY_OF_WEEK.ordinal)
        assertEquals(2, StatsGranularity.WEEK_OF_MONTH.ordinal)
        assertEquals(3, StatsGranularity.MONTH_OF_YEAR.ordinal)
    }
}
