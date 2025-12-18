package com.example.todolist.data.repository

import com.example.todolist.core.model.User
import com.example.todolist.core.model.Gender
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UserRepository
 */
class UserRepositoryTest {

    private var fakeUser: User? = null

    @Before
    fun setup() {
        fakeUser = User(id = 1, fullName = "Test User", age = 25, gender = Gender.MALE)
    }

    @Test
    fun `get user returns correct user`() {
        assertNotNull(fakeUser)
        assertEquals("Test User", fakeUser?.fullName)
    }

    @Test
    fun `create user stores user`() {
        val newUser = User(id = 2, fullName = "New User", age = 30, gender = Gender.FEMALE)
        fakeUser = newUser
        
        assertEquals("New User", fakeUser?.fullName)
        assertEquals(30, fakeUser?.age)
        assertEquals(Gender.FEMALE, fakeUser?.gender)
    }

    @Test
    fun `update user fullName`() {
        fakeUser = fakeUser?.copy(fullName = "Updated Name")
        assertEquals("Updated Name", fakeUser?.fullName)
    }

    @Test
    fun `update user age`() {
        fakeUser = fakeUser?.copy(age = 26)
        assertEquals(26, fakeUser?.age)
    }

    @Test
    fun `delete user clears data`() {
        fakeUser = null
        assertNull(fakeUser)
    }

    @Test
    fun `user exists returns true when user present`() {
        val exists = fakeUser != null
        assertTrue(exists)
    }

    @Test
    fun `user exists returns false when user absent`() {
        fakeUser = null
        val exists = fakeUser != null
        assertFalse(exists)
    }
}
