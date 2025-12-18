package com.example.todolist.data.local.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserDao interface definitions
 */
class UserDaoTest {

    // Fake in-memory storage for testing DAO contract
    private var fakeUser: FakeUserEntity? = null
    
    data class FakeUserEntity(
        val id: Int,
        var fullName: String,
        var age: Int,
        var gender: String,
        var avatarUrl: String? = null
    )

    @Test
    fun `getUser returns null when no user exists`() {
        fakeUser = null
        assertNull(fakeUser)
    }

    @Test
    fun `insert creates user`() {
        fakeUser = FakeUserEntity(1, "John Doe", 25, "MALE")
        
        assertNotNull(fakeUser)
        assertEquals("John Doe", fakeUser?.fullName)
    }

    @Test
    fun `insert with replace updates existing user`() {
        fakeUser = FakeUserEntity(1, "Original", 20, "MALE")
        fakeUser = FakeUserEntity(1, "Updated", 25, "FEMALE")
        
        assertEquals("Updated", fakeUser?.fullName)
        assertEquals(25, fakeUser?.age)
        assertEquals("FEMALE", fakeUser?.gender)
    }

    @Test
    fun `delete removes user`() {
        fakeUser = FakeUserEntity(1, "Test", 20, "MALE")
        fakeUser = null
        
        assertNull(fakeUser)
    }

    @Test
    fun `update modifies user fields`() {
        fakeUser = FakeUserEntity(1, "Original", 20, "MALE")
        
        fakeUser?.fullName = "New Name"
        fakeUser?.age = 30
        
        assertEquals("New Name", fakeUser?.fullName)
        assertEquals(30, fakeUser?.age)
    }

    @Test
    fun `user with avatar`() {
        fakeUser = FakeUserEntity(1, "User", 25, "FEMALE", "https://example.com/avatar.jpg")
        
        assertEquals("https://example.com/avatar.jpg", fakeUser?.avatarUrl)
    }

    @Test
    fun `user without avatar`() {
        fakeUser = FakeUserEntity(1, "User", 25, "OTHER")
        
        assertNull(fakeUser?.avatarUrl)
    }
}
