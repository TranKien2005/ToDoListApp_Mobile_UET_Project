package com.example.todolist.domain.usecase

import android.util.Log
import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.CommandAction
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.core.model.PendingCommand
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.UserContext
import com.example.todolist.domain.repository.AiRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Real implementation của ChatWithAIUseCase
 */
class RealChatWithAIUseCase(
    private val aiRepository: AiRepository
) : ChatWithAIUseCase {

    companion object {
        private const val TAG = "RealChatWithAI"
    }

    override suspend fun invoke(
        message: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> {
        Log.d(TAG, "Chatting with AI: $message")
        return aiRepository.chat(message, conversationHistory, userContext)
    }
}

/**
 * Real implementation của ChatWithAudioUseCase
 */
class RealChatWithAudioUseCase(
    private val aiRepository: AiRepository
) : ChatWithAudioUseCase {

    companion object {
        private const val TAG = "RealChatWithAudio"
    }

    override suspend fun invoke(
        audioBytes: ByteArray,
        mimeType: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> {
        Log.d(TAG, "Chatting with audio: ${audioBytes.size} bytes")
        return aiRepository.chatWithAudio(audioBytes, mimeType, conversationHistory, userContext)
    }
}

/**
 * Real implementation của ExecuteCommandUseCase
 * Thực thi command đã được confirm và trả về message
 */
class RealExecuteCommandUseCase(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) : ExecuteCommandUseCase {

    companion object {
        private const val TAG = "RealExecuteCommand"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    override suspend fun invoke(command: PendingCommand): Result<String> {
        return try {
            val action = command.toCommandAction()
            Log.d(TAG, "Executing command: $action")

            when (action) {
                CommandAction.CREATE_TASK -> {
                    createTask(command)
                    Result.success("Task đã được tạo thành công!")
                }
                CommandAction.DELETE_TASK -> {
                    deleteTask(command)
                    Result.success("Task đã được xóa.")
                }
                CommandAction.UPDATE_TASK -> {
                    updateTask(command)
                    Result.success("Task đã được cập nhật.")
                }
                CommandAction.CREATE_MISSION -> {
                    createMission(command)
                    Result.success("Mission đã được tạo thành công!")
                }
                CommandAction.DELETE_MISSION -> {
                    deleteMission(command)
                    Result.success("Mission đã được xóa.")
                }
                CommandAction.UPDATE_MISSION -> {
                    updateMission(command)
                    Result.success("Mission đã được cập nhật.")
                }
                CommandAction.COMPLETE_MISSION -> {
                    completeMission(command)
                    Result.success("Mission đã được đánh dấu hoàn thành!")
                }
                null -> {
                    Result.failure(Exception("Unknown command action: ${command.action}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            Result.failure(e)
        }
    }

    private suspend fun createTask(command: PendingCommand) {
        val params = command.params
        val date = params.date?.let { parseDate(it) } ?: LocalDate.now()
        val time = params.time?.let { parseTime(it) } ?: LocalTime.of(9, 0)
        val duration = params.duration?.toLong() ?: 60L

        val task = Task(
            id = 0,
            title = params.title ?: "Untitled Task",
            description = params.description,
            startTime = LocalDateTime.of(date, time),
            durationMinutes = duration,
            repeatType = RepeatType.NONE
        )

        taskUseCases.createTask(task)
        Log.d(TAG, "Task created: ${task.title}")
    }

    private suspend fun deleteTask(command: PendingCommand) {
        val params = command.params
        
        // Ưu tiên dùng taskId nếu có
        if (params.taskId != null) {
            taskUseCases.deleteTask(params.taskId)
            Log.d(TAG, "Task deleted by ID: ${params.taskId}")
            return
        }
        
        // Tìm theo title
        val title = params.title ?: throw Exception("Task title or ID is required")
        val tasks = taskUseCases.getTasks().first()
        val task = tasks.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Task not found: $title")

        taskUseCases.deleteTask(task.id)
        Log.d(TAG, "Task deleted: ${task.title}")
    }

    private suspend fun updateTask(command: PendingCommand) {
        val params = command.params
        val taskId = params.taskId ?: throw Exception("Task ID is required for update")
        
        val tasks = taskUseCases.getTasks().first()
        val existingTask = tasks.find { it.id == taskId }
            ?: throw Exception("Task not found: $taskId")

        val updatedTask = existingTask.copy(
            title = params.title ?: existingTask.title,
            description = params.description ?: existingTask.description,
            startTime = if (params.date != null || params.time != null) {
                val date = params.date?.let { parseDate(it) } ?: existingTask.startTime.toLocalDate()
                val time = params.time?.let { parseTime(it) } ?: existingTask.startTime.toLocalTime()
                LocalDateTime.of(date, time)
            } else existingTask.startTime,
            durationMinutes = params.duration?.toLong() ?: existingTask.durationMinutes
        )

        taskUseCases.updateTask(updatedTask)
        Log.d(TAG, "Task updated: ${updatedTask.title}")
    }

    private suspend fun createMission(command: PendingCommand) {
        val params = command.params
        val date = params.date?.let { parseDate(it) } ?: LocalDate.now().plusDays(7)
        val time = params.time?.let { parseTime(it) } ?: LocalTime.of(23, 59)

        val mission = Mission(
            id = 0,
            title = params.title ?: "Untitled Mission",
            description = params.description,
            deadline = LocalDateTime.of(date, time),
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        missionUseCases.createMission(mission)
        Log.d(TAG, "Mission created: ${mission.title}")
    }

    private suspend fun deleteMission(command: PendingCommand) {
        val params = command.params
        
        // Ưu tiên dùng missionId nếu có
        if (params.missionId != null) {
            missionUseCases.deleteMission(params.missionId)
            Log.d(TAG, "Mission deleted by ID: ${params.missionId}")
            return
        }
        
        // Tìm theo title
        val title = params.title ?: throw Exception("Mission title or ID is required")
        val missions = missionUseCases.getMissions().first()
        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Mission not found: $title")

        missionUseCases.deleteMission(mission.id)
        Log.d(TAG, "Mission deleted: ${mission.title}")
    }

    private suspend fun updateMission(command: PendingCommand) {
        val params = command.params
        val missionId = params.missionId ?: throw Exception("Mission ID is required for update")
        
        val missions = missionUseCases.getMissions().first()
        val existingMission = missions.find { it.id == missionId }
            ?: throw Exception("Mission not found: $missionId")

        val updatedMission = existingMission.copy(
            title = params.title ?: existingMission.title,
            description = params.description ?: existingMission.description,
            deadline = if (params.date != null || params.time != null) {
                val date = params.date?.let { parseDate(it) } ?: existingMission.deadline.toLocalDate()
                val time = params.time?.let { parseTime(it) } ?: existingMission.deadline.toLocalTime()
                LocalDateTime.of(date, time)
            } else existingMission.deadline
        )

        missionUseCases.updateMission(updatedMission)
        Log.d(TAG, "Mission updated: ${updatedMission.title}")
    }

    private suspend fun completeMission(command: PendingCommand) {
        val params = command.params
        
        // Ưu tiên dùng missionId nếu có
        if (params.missionId != null) {
            missionUseCases.setMissionStatus(params.missionId, MissionStoredStatus.COMPLETED)
            Log.d(TAG, "Mission completed by ID: ${params.missionId}")
            return
        }
        
        // Tìm theo title
        val title = params.title ?: throw Exception("Mission title or ID is required")
        val missions = missionUseCases.getMissions().first()
        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Mission not found: $title")

        missionUseCases.setMissionStatus(mission.id, MissionStoredStatus.COMPLETED)
        Log.d(TAG, "Mission completed: ${mission.title}")
    }

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateStr, using today")
            LocalDate.now()
        }
    }

    private fun parseTime(timeStr: String): LocalTime {
        return try {
            LocalTime.parse(timeStr, TIME_FORMATTER)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse time: $timeStr, using 09:00")
            LocalTime.of(9, 0)
        }
    }
}

/**
 * Factory function để tạo AIUseCases cho release builds
 */
fun createAIUseCases(
    aiRepository: AiRepository,
    taskUseCases: TaskUseCases,
    missionUseCases: MissionUseCases
): AIUseCases {
    return AIUseCases(
        chatWithAI = RealChatWithAIUseCase(aiRepository),
        chatWithAudio = RealChatWithAudioUseCase(aiRepository),
        executeCommand = RealExecuteCommandUseCase(taskUseCases, missionUseCases)
    )
}
