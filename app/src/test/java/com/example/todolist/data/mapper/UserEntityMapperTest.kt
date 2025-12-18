package com.example.todolist.data.mapper

import com.example.todolist.core.model.User
import com.example.todolist.core.model.Gender
import com.example.todolist.data.local.entity.UserEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserEntityMapper
 */
class UserEntityMapperTest {

    @Test
    fun `fromDomain converts User to UserEntity`() {
        val user = User(
            id = 1,
            fullName = "John Doe",
            age = 25,
            gender = Gender.MALE,
            avatarUrl = "https://example.com/avatar.jpg"
        )

        val entity = UserEntityMapper.fromDomain(user)

        assertEquals(1, entity.id)
        assertEquals("John Doe", entity.fullName)
        assertEquals(25, entity.age)
        assertEquals("MALE", entity.gender)
        assertEquals("https://example.com/avatar.jpg", entity.avatarUrl)
    }

    @Test
    fun `toDomain converts UserEntity to User`() {
        val entity = UserEntity(
            id = 2,
            fullName = "Jane Doe",
            age = 30,
            gender = "FEMALE",
            avatarUrl = null
        )

        val user = UserEntityMapper.toDomain(entity)

        assertEquals(2, user.id)
        assertEquals("Jane Doe", user.fullName)
        assertEquals(30, user.age)
        assertEquals(Gender.FEMALE, user.gender)
        assertNull(user.avatarUrl)
    }

    @Test
    fun `toDomain handles invalid gender`() {
        val entity = UserEntity(
            id = 1,
            fullName = "Test",
            age = 20,
            gender = "INVALID"
        )

        val user = UserEntityMapper.toDomain(entity)

        assertEquals(Gender.OTHER, user.gender)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalUser = User(
            id = 5,
            fullName = "Round Trip User",
            age = 35,
            gender = Gender.OTHER,
            avatarUrl = "http://image.url"
        )

        val entity = UserEntityMapper.fromDomain(originalUser)
        val convertedUser = UserEntityMapper.toDomain(entity)

        assertEquals(originalUser.id, convertedUser.id)
        assertEquals(originalUser.fullName, convertedUser.fullName)
        assertEquals(originalUser.age, convertedUser.age)
        assertEquals(originalUser.gender, convertedUser.gender)
        assertEquals(originalUser.avatarUrl, convertedUser.avatarUrl)
    }

    @Test
    fun `fromDomain handles all genders`() {
        listOf(Gender.MALE, Gender.FEMALE, Gender.OTHER).forEach { gender ->
            val user = User(fullName = "Test", age = 20, gender = gender)
            val entity = UserEntityMapper.fromDomain(user)
            assertEquals(gender.name, entity.gender)
        }
    }
}
