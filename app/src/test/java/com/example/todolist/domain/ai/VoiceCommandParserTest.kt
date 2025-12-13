package com.example.todolist.domain.ai

import com.example.todolist.domain.ai.models.CommandParams
import com.example.todolist.domain.ai.models.VoiceAction
import com.example.todolist.domain.ai.models.VoiceCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class VoiceCommandParserTest {

    private lateinit var parser: VoiceCommandParser

    @Before
    fun setup() {
        parser = VoiceCommandParser()
    }

    // ============ parseResponse tests ============

    @Test
    fun `parseResponse successfully parses valid JSON`() {
        val jsonResponse = """
            {
                "action": "CREATE_TASK",
                "params": {
                    "title": "Meeting with team",
                    "date": "15/12/2024",
                    "time": "14:00",
                    "duration": 60
                },
                "response_text": "Đã tạo task Meeting with team"
            }
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals(VoiceAction.CREATE_TASK, command.action)
        assertEquals("Meeting with team", command.params.title)
        assertEquals("15/12/2024", command.params.date)
        assertEquals("14:00", command.params.time)
        assertEquals(60, command.params.duration)
        assertEquals("Đã tạo task Meeting with team", command.responseText)
    }

    @Test
    fun `parseResponse handles JSON with markdown code blocks`() {
        val jsonResponse = """
            ```json
            {
                "action": "CREATE_MISSION",
                "params": {
                    "title": "Complete project"
                },
                "response_text": "Đã tạo mission"
            }
            ```
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals(VoiceAction.CREATE_MISSION, command.action)
        assertEquals("Complete project", command.params.title)
    }

    @Test
    fun `parseResponse handles JSON with simple markdown code blocks`() {
        val jsonResponse = """
            ```
            {
                "action": "LIST_TASKS",
                "params": {},
                "response_text": "Listing tasks"
            }
            ```
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals(VoiceAction.LIST_TASKS, command.action)
    }

    @Test
    fun `parseResponse returns failure for invalid JSON`() {
        val invalidJson = "not valid json at all"

        val result = parser.parseResponse(invalidJson)

        assertTrue(result.isFailure)
    }

    @Test
    fun `parseResponse returns failure for incomplete JSON`() {
        val incompleteJson = """{"action": "CREATE_TASK"""

        val result = parser.parseResponse(incompleteJson)

        assertTrue(result.isFailure)
    }

    @Test
    fun `parseResponse handles unknown action as UNKNOWN`() {
        val jsonResponse = """
            {
                "action": "SOME_FUTURE_ACTION",
                "params": {},
                "response_text": "Unknown action"
            }
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals(VoiceAction.UNKNOWN, command.action)
    }

    @Test
    fun `parseResponse handles lowercase action names`() {
        val jsonResponse = """
            {
                "action": "create_task",
                "params": {"title": "Test"},
                "response_text": "Test response"
            }
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals(VoiceAction.CREATE_TASK, command.action)
    }

    @Test
    fun `parseResponse handles all valid action types`() {
        val actionTypes = listOf(
            "CREATE_TASK", "CREATE_MISSION", "LIST_TASKS", "LIST_MISSIONS",
            "COMPLETE_TASK", "COMPLETE_MISSION", "DELETE_TASK", "DELETE_MISSION",
            "UPDATE_TASK", "UPDATE_MISSION"
        )

        actionTypes.forEach { actionStr ->
            val jsonResponse = """
                {
                    "action": "$actionStr",
                    "params": {},
                    "response_text": "Response for $actionStr"
                }
            """.trimIndent()

            val result = parser.parseResponse(jsonResponse)
            assertTrue("Failed to parse action: $actionStr", result.isSuccess)
        }
    }

    @Test
    fun `parseResponse handles optional params fields`() {
        val jsonResponse = """
            {
                "action": "CREATE_TASK",
                "params": {
                    "title": "Only title provided"
                },
                "response_text": "Task created"
            }
        """.trimIndent()

        val result = parser.parseResponse(jsonResponse)

        assertTrue(result.isSuccess)
        val command = result.getOrNull()!!
        assertEquals("Only title provided", command.params.title)
        assertEquals(null, command.params.description)
        assertEquals(null, command.params.date)
        assertEquals(null, command.params.time)
        assertEquals(null, command.params.duration)
    }

    // ============ validateCommand tests ============

    @Test
    fun `validateCommand CREATE_TASK requires title`() {
        val commandWithoutTitle = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithoutTitle)

        assertTrue(result.isFailure)
        assertEquals("Task title is required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateCommand CREATE_TASK succeeds with title`() {
        val commandWithTitle = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = "Valid Task"),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithTitle)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand CREATE_TASK fails with blank title`() {
        val commandWithBlankTitle = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = "   "),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithBlankTitle)

        assertTrue(result.isFailure)
    }

    @Test
    fun `validateCommand CREATE_MISSION requires title`() {
        val commandWithoutTitle = VoiceCommand(
            action = VoiceAction.CREATE_MISSION,
            params = CommandParams(title = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithoutTitle)

        assertTrue(result.isFailure)
        assertEquals("Mission title is required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateCommand CREATE_MISSION succeeds with title`() {
        val commandWithTitle = VoiceCommand(
            action = VoiceAction.CREATE_MISSION,
            params = CommandParams(title = "Valid Mission"),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithTitle)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand COMPLETE_TASK requires title or taskId`() {
        val commandWithNeither = VoiceCommand(
            action = VoiceAction.COMPLETE_TASK,
            params = CommandParams(title = null, taskId = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithNeither)

        assertTrue(result.isFailure)
        assertEquals("Task title or ID is required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateCommand COMPLETE_TASK succeeds with title`() {
        val commandWithTitle = VoiceCommand(
            action = VoiceAction.COMPLETE_TASK,
            params = CommandParams(title = "Task to complete"),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithTitle)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand COMPLETE_TASK succeeds with taskId`() {
        val commandWithId = VoiceCommand(
            action = VoiceAction.COMPLETE_TASK,
            params = CommandParams(taskId = 42),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithId)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand DELETE_TASK requires title or taskId`() {
        val commandWithNeither = VoiceCommand(
            action = VoiceAction.DELETE_TASK,
            params = CommandParams(title = null, taskId = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithNeither)

        assertTrue(result.isFailure)
    }

    @Test
    fun `validateCommand COMPLETE_MISSION requires title or missionId`() {
        val commandWithNeither = VoiceCommand(
            action = VoiceAction.COMPLETE_MISSION,
            params = CommandParams(title = null, missionId = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithNeither)

        assertTrue(result.isFailure)
        assertEquals("Mission title or ID is required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateCommand COMPLETE_MISSION succeeds with missionId`() {
        val commandWithId = VoiceCommand(
            action = VoiceAction.COMPLETE_MISSION,
            params = CommandParams(missionId = 99),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithId)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand DELETE_MISSION requires title or missionId`() {
        val commandWithNeither = VoiceCommand(
            action = VoiceAction.DELETE_MISSION,
            params = CommandParams(title = null, missionId = null),
            responseText = "Response"
        )

        val result = parser.validateCommand(commandWithNeither)

        assertTrue(result.isFailure)
    }

    @Test
    fun `validateCommand LIST_TASKS always succeeds`() {
        val command = VoiceCommand(
            action = VoiceAction.LIST_TASKS,
            params = CommandParams(),
            responseText = "Response"
        )

        val result = parser.validateCommand(command)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand LIST_MISSIONS always succeeds`() {
        val command = VoiceCommand(
            action = VoiceAction.LIST_MISSIONS,
            params = CommandParams(),
            responseText = "Response"
        )

        val result = parser.validateCommand(command)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateCommand UNKNOWN always succeeds`() {
        val command = VoiceCommand(
            action = VoiceAction.UNKNOWN,
            params = CommandParams(),
            responseText = "Response"
        )

        val result = parser.validateCommand(command)

        assertTrue(result.isSuccess)
    }
}
