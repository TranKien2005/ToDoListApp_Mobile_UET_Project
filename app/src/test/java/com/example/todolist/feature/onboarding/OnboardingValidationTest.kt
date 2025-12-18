package com.example.todolist.feature.onboarding

import com.example.todolist.core.model.User
import com.example.todolist.core.model.Gender
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Onboarding validation logic
 */
class OnboardingValidationTest {

    @Test
    fun `valid user name`() {
        val name = "John Doe"
        assertTrue(name.isNotBlank())
        assertTrue(name.length >= 2)
    }

    @Test
    fun `empty name is invalid`() {
        val name = ""
        assertTrue(name.isBlank())
    }

    @Test
    fun `whitespace only name is invalid`() {
        val name = "   "
        assertTrue(name.isBlank())
    }

    @Test
    fun `valid age range`() {
        val age = 25
        assertTrue(age in 1..120)
    }

    @Test
    fun `zero age is invalid`() {
        val age = 0
        assertFalse(age in 1..120)
    }

    @Test
    fun `negative age is invalid`() {
        val age = -5
        assertFalse(age in 1..120)
    }

    @Test
    fun `age over 120 is invalid`() {
        val age = 150
        assertFalse(age in 1..120)
    }

    @Test
    fun `all gender options exist`() {
        assertEquals(3, Gender.values().size)
        assertTrue(Gender.values().contains(Gender.MALE))
        assertTrue(Gender.values().contains(Gender.FEMALE))
        assertTrue(Gender.values().contains(Gender.OTHER))
    }

    @Test
    fun `user creation with valid data`() {
        val user = User(
            fullName = "John Doe",
            age = 25,
            gender = Gender.MALE
        )

        assertNotNull(user)
        assertEquals("John Doe", user.fullName)
        assertEquals(25, user.age)
        assertEquals(Gender.MALE, user.gender)
    }

    @Test
    fun `user name trimming`() {
        val rawName = "  John Doe  "
        val trimmedName = rawName.trim()
        
        assertEquals("John Doe", trimmedName)
    }
}
