package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RepeatType enum
 */
class RepeatTypeTest {

    @Test
    fun `all repeat types exist`() {
        val types = RepeatType.values()
        assertEquals(4, types.size)
    }

    @Test
    fun `repeat type NONE`() {
        assertEquals(RepeatType.NONE, RepeatType.valueOf("NONE"))
    }

    @Test
    fun `repeat type DAILY`() {
        assertEquals(RepeatType.DAILY, RepeatType.valueOf("DAILY"))
    }

    @Test
    fun `repeat type WEEKLY`() {
        assertEquals(RepeatType.WEEKLY, RepeatType.valueOf("WEEKLY"))
    }

    @Test
    fun `repeat type MONTHLY`() {
        assertEquals(RepeatType.MONTHLY, RepeatType.valueOf("MONTHLY"))
    }

    @Test
    fun `repeat type ordinals are correct`() {
        assertEquals(0, RepeatType.NONE.ordinal)
        assertEquals(1, RepeatType.DAILY.ordinal)
        assertEquals(2, RepeatType.WEEKLY.ordinal)
        assertEquals(3, RepeatType.MONTHLY.ordinal)
    }
}
