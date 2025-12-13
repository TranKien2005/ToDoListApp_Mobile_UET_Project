package com.example.todolist.util.extension

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class DateExtTest {

    @Test
    fun `toLocalDateTime converts epoch millis correctly`() {
        val expected = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
        val epochMillis = expected.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val result = DateExt.toLocalDateTime(epochMillis)
        
        assertEquals(expected.year, result.year)
        assertEquals(expected.monthValue, result.monthValue)
        assertEquals(expected.dayOfMonth, result.dayOfMonth)
        assertEquals(expected.hour, result.hour)
        assertEquals(expected.minute, result.minute)
    }

    @Test
    fun `toEpochMillis converts datetime correctly`() {
        val dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
        val expected = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val result = DateExt.toEpochMillis(dateTime)
        
        assertEquals(expected, result)
    }

    @Test
    fun `roundtrip conversion preserves datetime`() {
        val original = LocalDateTime.of(2024, 12, 25, 23, 59, 59)
        
        val epochMillis = DateExt.toEpochMillis(original)
        val result = DateExt.toLocalDateTime(epochMillis)
        
        assertEquals(original.year, result.year)
        assertEquals(original.monthValue, result.monthValue)
        assertEquals(original.dayOfMonth, result.dayOfMonth)
        assertEquals(original.hour, result.hour)
        assertEquals(original.minute, result.minute)
        assertEquals(original.second, result.second)
    }

    @Test
    fun `epoch 0 converts to 1970 datetime`() {
        val result = DateExt.toLocalDateTime(0L)
        
        // Will be 1970-01-01 in local timezone (offset depends on zone)
        assertEquals(1970, result.year)
        assertEquals(1, result.monthValue)
    }

    @Test
    fun `midnight datetime converts correctly`() {
        val midnight = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
        
        val epochMillis = DateExt.toEpochMillis(midnight)
        val result = DateExt.toLocalDateTime(epochMillis)
        
        assertEquals(0, result.hour)
        assertEquals(0, result.minute)
        assertEquals(0, result.second)
    }

    @Test
    fun `end of day datetime converts correctly`() {
        val endOfDay = LocalDateTime.of(2024, 12, 31, 23, 59, 59)
        
        val epochMillis = DateExt.toEpochMillis(endOfDay)
        val result = DateExt.toLocalDateTime(epochMillis)
        
        assertEquals(23, result.hour)
        assertEquals(59, result.minute)
        assertEquals(59, result.second)
    }

    @Test
    fun `future date converts correctly`() {
        val futureDate = LocalDateTime.of(2099, 12, 31, 12, 0, 0)
        
        val epochMillis = DateExt.toEpochMillis(futureDate)
        val result = DateExt.toLocalDateTime(epochMillis)
        
        assertEquals(2099, result.year)
        assertEquals(12, result.monthValue)
        assertEquals(31, result.dayOfMonth)
    }

    @Test
    fun `different months have different epoch values`() {
        val jan = LocalDateTime.of(2024, 1, 15, 12, 0)
        val dec = LocalDateTime.of(2024, 12, 15, 12, 0)
        
        val janEpoch = DateExt.toEpochMillis(jan)
        val decEpoch = DateExt.toEpochMillis(dec)
        
        // December should have larger epoch value
        assertEquals(true, decEpoch > janEpoch)
    }
}
