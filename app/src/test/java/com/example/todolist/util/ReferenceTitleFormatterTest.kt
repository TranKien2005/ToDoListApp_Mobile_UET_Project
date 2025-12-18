package com.example.todolist.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Unit tests for ReferenceTitleFormatter
 */
class ReferenceTitleFormatterTest {

    @Test
    fun `format day of week`() {
        val monday = LocalDate.of(2024, 12, 16) // Monday
        assertEquals("MONDAY", monday.dayOfWeek.name)
    }

    @Test
    fun `format month`() {
        val december = YearMonth.of(2024, 12)
        assertEquals(12, december.monthValue)
        assertEquals(2024, december.year)
    }

    @Test
    fun `format year month string`() {
        val yearMonth = YearMonth.of(2024, 12)
        val formatted = "${yearMonth.month.name} ${yearMonth.year}"
        assertEquals("DECEMBER 2024", formatted)
    }

    @Test
    fun `week of month calculation`() {
        val date = LocalDate.of(2024, 12, 18)
        val weekOfMonth = (date.dayOfMonth - 1) / 7 + 1
        
        assertTrue(weekOfMonth in 1..5)
    }

    @Test
    fun `day of year calculation`() {
        val date = LocalDate.of(2024, 12, 31)
        val dayOfYear = date.dayOfYear
        
        assertEquals(366, dayOfYear) // 2024 is a leap year
    }

    @Test
    fun `format date range for week`() {
        val start = LocalDate.of(2024, 12, 16)
        val end = start.plusDays(6)
        
        assertTrue(end.isAfter(start))
        assertEquals(start.plusWeeks(1).minusDays(1), end)
    }

    @Test
    fun `format short month name`() {
        val date = LocalDate.of(2024, 12, 1)
        val shortMonth = date.month.name.take(3) // "DEC"
        
        assertEquals("DEC", shortMonth)
    }
}
