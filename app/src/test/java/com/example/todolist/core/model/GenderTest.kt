package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Gender enum
 */
class GenderTest {

    @Test
    fun `gender enum has all values`() {
        val genders = Gender.values()
        assertEquals(3, genders.size)
    }

    @Test
    fun `gender male exists`() {
        assertEquals(Gender.MALE, Gender.valueOf("MALE"))
    }

    @Test
    fun `gender female exists`() {
        assertEquals(Gender.FEMALE, Gender.valueOf("FEMALE"))
    }

    @Test
    fun `gender other exists`() {
        assertEquals(Gender.OTHER, Gender.valueOf("OTHER"))
    }

    @Test
    fun `gender ordinal values`() {
        assertEquals(0, Gender.MALE.ordinal)
        assertEquals(1, Gender.FEMALE.ordinal)
        assertEquals(2, Gender.OTHER.ordinal)
    }

    @Test
    fun `gender name values`() {
        assertEquals("MALE", Gender.MALE.name)
        assertEquals("FEMALE", Gender.FEMALE.name)
        assertEquals("OTHER", Gender.OTHER.name)
    }
}
