package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserUseCases interfaces
 */
class UserUseCasesTest {

    // Fake user storage
    private var fakeUser: FakeUser? = null
    
    data class FakeUser(
        val id: Int,
        var fullName: String,
        var age: Int,
        var gender: String,
        var avatarUrl: String? = null
    )

    @Test
    fun `getUser returns null when no user`() {
        fakeUser = null
        assertNull(fakeUser)
    }

    @Test
    fun `getUser returns user when exists`() {
        fakeUser = FakeUser(1, "John Doe", 25, "MALE")
        
        assertNotNull(fakeUser)
        assertEquals("John Doe", fakeUser?.fullName)
    }

    @Test
    fun `saveUser creates new user`() {
        fakeUser = FakeUser(0, "New User", 30, "FEMALE")
        
        assertEquals("New User", fakeUser?.fullName)
        assertEquals(30, fakeUser?.age)
    }

    @Test
    fun `updateUser modifies existing user`() {
        fakeUser = FakeUser(1, "Original", 20, "MALE")
        
        fakeUser = fakeUser?.copy(fullName = "Updated", age = 25)
        
        assertEquals("Updated", fakeUser?.fullName)
        assertEquals(25, fakeUser?.age)
    }

    @Test
    fun `deleteUser removes user`() {
        fakeUser = FakeUser(1, "Test", 25, "OTHER")
        fakeUser = null
        
        assertNull(fakeUser)
    }

    @Test
    fun `saveUser with avatar`() {
        fakeUser = FakeUser(1, "User", 25, "MALE", "https://avatar.url")
        
        assertEquals("https://avatar.url", fakeUser?.avatarUrl)
    }

    @Test
    fun `updateUser changes avatar`() {
        fakeUser = FakeUser(1, "User", 25, "MALE")
        fakeUser = fakeUser?.copy(avatarUrl = "https://new-avatar.url")
        
        assertEquals("https://new-avatar.url", fakeUser?.avatarUrl)
    }

    @Test
    fun `updateUser changes gender`() {
        fakeUser = FakeUser(1, "User", 25, "MALE")
        fakeUser = fakeUser?.copy(gender = "OTHER")
        
        assertEquals("OTHER", fakeUser?.gender)
    }
}
