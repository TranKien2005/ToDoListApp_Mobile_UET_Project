package com.example.todolist.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.DayOfWeek

/**
 * Unit tests for date utilities and extensions
 */
class DateUtilsTest {

    @Test
    fun `localDate today is not in past`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        assertTrue(today.isAfter(yesterday))
        assertFalse(today.isBefore(yesterday))
    }

    @Test
    fun `localDateTime comparison works correctly`() {
        val now = LocalDateTime.now()
        val future = now.plusHours(1)
        val past = now.minusHours(1)

        assertTrue(future.isAfter(now))
        assertTrue(past.isBefore(now))
    }

    @Test
    fun `yearMonth comparison works correctly`() {
        val currentMonth = YearMonth.now()
        val nextMonth = currentMonth.plusMonths(1)
        val lastMonth = currentMonth.minusMonths(1)

        assertTrue(nextMonth.isAfter(currentMonth))
        assertTrue(lastMonth.isBefore(currentMonth))
    }

    @Test
    fun `week calculation is correct`() {
        val monday = LocalDate.of(2024, 12, 16) // Monday
        val sunday = monday.plusDays(6)
        
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
    }

    @Test
    fun `month has correct number of days`() {
        val december2024 = YearMonth.of(2024, 12)
        val february2024 = YearMonth.of(2024, 2) // Leap year

        assertEquals(31, december2024.lengthOfMonth())
        assertEquals(29, february2024.lengthOfMonth()) // Leap year
    }

    @Test
    fun `date arithmetic works correctly`() {
        val date = LocalDate.of(2024, 12, 15)
        
        assertEquals(LocalDate.of(2024, 12, 22), date.plusWeeks(1))
        assertEquals(LocalDate.of(2025, 1, 15), date.plusMonths(1))
        assertEquals(LocalDate.of(2025, 12, 15), date.plusYears(1))
    }

    @Test
    fun `get first day of week`() {
        val date = LocalDate.of(2024, 12, 18) // Wednesday
        val firstDayOfWeek = date.with(DayOfWeek.MONDAY)
        
        assertEquals(DayOfWeek.MONDAY, firstDayOfWeek.dayOfWeek)
        assertEquals(LocalDate.of(2024, 12, 16), firstDayOfWeek)
    }

    @Test
    fun `get last day of month`() {
        val december = LocalDate.of(2024, 12, 15)
        val lastDay = december.withDayOfMonth(december.lengthOfMonth())
        
        assertEquals(31, lastDay.dayOfMonth)
    }

    @Test
    fun `epoch conversion works`() {
        val now = System.currentTimeMillis()
        val later = now + 3600000 // 1 hour later
        
        assertTrue(later > now)
        assertEquals(3600000, later - now)
    }
}
