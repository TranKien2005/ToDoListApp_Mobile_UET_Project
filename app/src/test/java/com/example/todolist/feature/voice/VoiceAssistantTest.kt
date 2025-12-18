package com.example.todolist.feature.voice

import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.ChatRole
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for VoiceAssistant related functionality
 */
class VoiceAssistantTest {

    @Test
    fun `create chat message from user`() {
        val message = ChatMessage(
            role = ChatRole.USER,
            content = "Hello"
        )

        assertEquals(ChatRole.USER, message.role)
        assertEquals("Hello", message.content)
    }

    @Test
    fun `create chat message from assistant`() {
        val message = ChatMessage(
            role = ChatRole.ASSISTANT,
            content = "Hi, how can I help?"
        )

        assertEquals(ChatRole.ASSISTANT, message.role)
    }

    @Test
    fun `chat messages have timestamps`() {
        val before = System.currentTimeMillis()
        val message = ChatMessage(
            role = ChatRole.USER,
            content = "Test"
        )
        val after = System.currentTimeMillis()

        assertTrue(message.timestamp >= before)
        assertTrue(message.timestamp <= after)
    }

    @Test
    fun `chat role enum values`() {
        assertEquals(2, ChatRole.values().size)
        assertTrue(ChatRole.values().contains(ChatRole.USER))
        assertTrue(ChatRole.values().contains(ChatRole.ASSISTANT))
    }

    @Test
    fun `filter user messages`() {
        val messages = listOf(
            ChatMessage(role = ChatRole.USER, content = "User 1"),
            ChatMessage(role = ChatRole.ASSISTANT, content = "Bot 1"),
            ChatMessage(role = ChatRole.USER, content = "User 2")
        )

        val userMessages = messages.filter { it.role == ChatRole.USER }
        assertEquals(2, userMessages.size)
    }

    @Test
    fun `message default id is timestamp`() {
        val message = ChatMessage(
            role = ChatRole.USER,
            content = "Test"
        )

        assertTrue(message.id > 0)
    }
}
