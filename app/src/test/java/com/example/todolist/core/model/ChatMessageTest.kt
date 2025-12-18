package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChatMessage and related types
 */
class ChatMessageTest {

    @Test
    fun `create chat message from user`() {
        val message = ChatMessage(
            role = ChatRole.USER,
            content = "Hello"
        )

        assertEquals(ChatRole.USER, message.role)
        assertEquals("Hello", message.content)
        assertNull(message.pendingCommand)
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
    fun `chat role enum values`() {
        assertEquals(2, ChatRole.values().size)
        assertEquals(ChatRole.USER, ChatRole.valueOf("USER"))
        assertEquals(ChatRole.ASSISTANT, ChatRole.valueOf("ASSISTANT"))
    }

    @Test
    fun `command action enum values`() {
        val actions = CommandAction.values()
        assertEquals(7, actions.size)
        assertTrue(actions.contains(CommandAction.CREATE_TASK))
        assertTrue(actions.contains(CommandAction.DELETE_TASK))
        assertTrue(actions.contains(CommandAction.UPDATE_TASK))
        assertTrue(actions.contains(CommandAction.CREATE_MISSION))
        assertTrue(actions.contains(CommandAction.DELETE_MISSION))
        assertTrue(actions.contains(CommandAction.UPDATE_MISSION))
        assertTrue(actions.contains(CommandAction.COMPLETE_MISSION))
    }

    @Test
    fun `command params creation`() {
        val params = CommandParams(
            title = "New Task",
            description = "Description",
            date = "25/12/2024",
            time = "10:00",
            duration = 60
        )

        assertEquals("New Task", params.title)
        assertEquals("Description", params.description)
        assertEquals("25/12/2024", params.date)
        assertEquals("10:00", params.time)
        assertEquals(60, params.duration)
    }

    @Test
    fun `command params default values`() {
        val params = CommandParams()

        assertNull(params.title)
        assertNull(params.description)
        assertNull(params.date)
        assertNull(params.time)
        assertNull(params.duration)
        assertNull(params.taskId)
        assertNull(params.missionId)
    }

    @Test
    fun `pending command to action conversion`() {
        val pending = PendingCommand(
            action = "CREATE_TASK",
            params = CommandParams(title = "Test"),
            confirmationMessage = "Create task?"
        )

        assertEquals(CommandAction.CREATE_TASK, pending.toCommandAction())
    }

    @Test
    fun `pending command invalid action returns null`() {
        val pending = PendingCommand(
            action = "INVALID_ACTION",
            params = CommandParams(),
            confirmationMessage = "Invalid?"
        )

        assertNull(pending.toCommandAction())
    }

    @Test
    fun `ai chat response creation`() {
        val response = AiChatResponse(
            message = "I'll create that for you",
            pending_command = PendingCommand(
                action = "CREATE_TASK",
                params = CommandParams(title = "Test"),
                confirmationMessage = "Create?"
            )
        )

        assertEquals("I'll create that for you", response.message)
        assertNotNull(response.pending_command)
    }

    @Test
    fun `ai chat response without command`() {
        val response = AiChatResponse(
            message = "Hello!"
        )

        assertEquals("Hello!", response.message)
        assertNull(response.pending_command)
    }
}
