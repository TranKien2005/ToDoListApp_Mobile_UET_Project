package com.example.todolist.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserEntity
 */
class UserEntityTest {

    @Test
    fun `create user entity with all fields`() {
        val entity = UserEntity(
            id = 1,
            fullName = "John Doe",
            age = 25,
            gender = "MALE",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        assertEquals(1, entity.id)
        assertEquals("John Doe", entity.fullName)
        assertEquals(25, entity.age)
        assertEquals("MALE", entity.gender)
        assertEquals("https://example.com/avatar.jpg", entity.avatarUrl)
    }

    @Test
    fun `create user entity with null avatar`() {
        val entity = UserEntity(
            id = 0,
            fullName = "Jane Doe",
            age = 30,
            gender = "FEMALE"
        )

        assertNull(entity.avatarUrl)
    }

    @Test
    fun `user entity equality`() {
        val entity1 = UserEntity(id = 1, fullName = "Test", age = 20, gender = "MALE")
        val entity2 = UserEntity(id = 1, fullName = "Test", age = 20, gender = "MALE")
        
        assertEquals(entity1, entity2)
    }
}
