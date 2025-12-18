package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for User model
 */
class UserTest {

    @Test
    fun `create user with all fields`() {
        val user = User(
            id = 1,
            fullName = "John Doe",
            age = 25,
            gender = Gender.MALE,
            avatarUrl = "https://example.com/avatar.jpg"
        )

        assertEquals(1, user.id)
        assertEquals("John Doe", user.fullName)
        assertEquals(25, user.age)
        assertEquals(Gender.MALE, user.gender)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
    }

    @Test
    fun `create user with default avatar`() {
        val user = User(
            fullName = "Jane Doe",
            age = 30,
            gender = Gender.FEMALE
        )

        assertEquals(0, user.id) // default id
        assertEquals("Jane Doe", user.fullName)
        assertEquals(30, user.age)
        assertEquals(Gender.FEMALE, user.gender)
        assertNull(user.avatarUrl)
    }

    @Test
    fun `gender enum values`() {
        assertEquals(3, Gender.values().size)
        assertEquals(Gender.MALE, Gender.valueOf("MALE"))
        assertEquals(Gender.FEMALE, Gender.valueOf("FEMALE"))
        assertEquals(Gender.OTHER, Gender.valueOf("OTHER"))
    }

    @Test
    fun `user equality test`() {
        val user1 = User(id = 1, fullName = "Test", age = 20, gender = Gender.MALE)
        val user2 = User(id = 1, fullName = "Test", age = 20, gender = Gender.MALE)
        
        assertEquals(user1, user2)
    }
}
