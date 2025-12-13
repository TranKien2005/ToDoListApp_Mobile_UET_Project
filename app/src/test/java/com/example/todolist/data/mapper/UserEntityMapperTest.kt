package com.example.todolist.data.mapper

import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.User
import com.example.todolist.data.local.entity.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserEntityMapperTest {

    @Test
    fun `toDomain maps entity to user correctly`() {
        val entity = UserEntity(
            id = 1,
            fullName = "John Doe",
            age = 30,
            gender = "MALE",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        
        val user = UserEntityMapper.toDomain(entity)
        
        assertEquals(1, user.id)
        assertEquals("John Doe", user.fullName)
        assertEquals(30, user.age)
        assertEquals(Gender.MALE, user.gender)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
    }

    @Test
    fun `fromDomain maps user to entity correctly`() {
        val user = User(
            id = 1,
            fullName = "Jane Doe",
            age = 25,
            gender = Gender.FEMALE,
            avatarUrl = "https://example.com/jane.jpg"
        )
        
        val entity = UserEntityMapper.fromDomain(user)
        
        assertEquals(1, entity.id)
        assertEquals("Jane Doe", entity.fullName)
        assertEquals(25, entity.age)
        assertEquals("FEMALE", entity.gender)
        assertEquals("https://example.com/jane.jpg", entity.avatarUrl)
    }

    @Test
    fun `roundtrip mapping preserves all fields`() {
        val original = User(
            id = 42,
            fullName = "Test User",
            age = 28,
            gender = Gender.OTHER,
            avatarUrl = "https://example.com/test.jpg"
        )
        
        val entity = UserEntityMapper.fromDomain(original)
        val mapped = UserEntityMapper.toDomain(entity)
        
        assertEquals(original.id, mapped.id)
        assertEquals(original.fullName, mapped.fullName)
        assertEquals(original.age, mapped.age)
        assertEquals(original.gender, mapped.gender)
        assertEquals(original.avatarUrl, mapped.avatarUrl)
    }

    @Test
    fun `null avatar URL is preserved`() {
        val user = User(
            id = 1,
            fullName = "No Avatar",
            age = 20,
            gender = Gender.MALE,
            avatarUrl = null
        )
        
        val entity = UserEntityMapper.fromDomain(user)
        val mapped = UserEntityMapper.toDomain(entity)
        
        assertNull(mapped.avatarUrl)
    }

    @Test
    fun `invalid gender defaults to OTHER`() {
        val entity = UserEntity(
            id = 1,
            fullName = "Unknown Gender",
            age = 25,
            gender = "INVALID_GENDER",
            avatarUrl = null
        )
        
        val user = UserEntityMapper.toDomain(entity)
        
        assertEquals(Gender.OTHER, user.gender)
    }

    @Test
    fun `all genders are properly converted`() {
        Gender.values().forEach { gender ->
            val user = User(
                id = 1,
                fullName = "Test",
                age = 25,
                gender = gender,
                avatarUrl = null
            )
            
            val entity = UserEntityMapper.fromDomain(user)
            val mapped = UserEntityMapper.toDomain(entity)
            
            assertEquals(gender, mapped.gender)
        }
    }

    @Test
    fun `MALE gender is mapped correctly`() {
        val entity = UserEntity(
            id = 1,
            fullName = "Male User",
            age = 30,
            gender = "MALE",
            avatarUrl = null
        )
        
        val user = UserEntityMapper.toDomain(entity)
        assertEquals(Gender.MALE, user.gender)
    }

    @Test
    fun `FEMALE gender is mapped correctly`() {
        val entity = UserEntity(
            id = 1,
            fullName = "Female User",
            age = 30,
            gender = "FEMALE",
            avatarUrl = null
        )
        
        val user = UserEntityMapper.toDomain(entity)
        assertEquals(Gender.FEMALE, user.gender)
    }

    @Test
    fun `OTHER gender is mapped correctly`() {
        val entity = UserEntity(
            id = 1,
            fullName = "Other User",
            age = 30,
            gender = "OTHER",
            avatarUrl = null
        )
        
        val user = UserEntityMapper.toDomain(entity)
        assertEquals(Gender.OTHER, user.gender)
    }

    @Test
    fun `id zero is preserved for new users`() {
        val user = User(
            id = 0,
            fullName = "New User",
            age = 18,
            gender = Gender.MALE,
            avatarUrl = null
        )
        
        val entity = UserEntityMapper.fromDomain(user)
        val mapped = UserEntityMapper.toDomain(entity)
        
        assertEquals(0, mapped.id)
    }
}
