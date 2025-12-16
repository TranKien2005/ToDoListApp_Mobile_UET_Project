package com.example.todolist.domain.usecase

import android.util.Log
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.VoiceAction
import com.example.todolist.core.model.VoiceCommand
import com.example.todolist.domain.repository.AiRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Real implementation of ProcessVoiceCommandUseCase
 */
class RealProcessVoiceCommandUseCase(
    private val aiRepository: AiRepository
) : ProcessVoiceCommandUseCase {

    companion object {
        private const val TAG = "RealProcessVoiceCmd"
    }

    override suspend fun invoke(userInput: String): Result<VoiceCommand> {
        Log.d(TAG, "Processing voice input: $userInput")
        return aiRepository.processTextCommand(userInput)
    }
}

/**
 * Real implementation of ProcessAudioCommandUseCase
 */
class RealProcessAudioCommandUseCase(
    private val aiRepository: AiRepository
) : ProcessAudioCommandUseCase {

    companion object {
        private const val TAG = "RealProcessAudioCmd"
    }

    override suspend fun invoke(audioBytes: ByteArray, mimeType: String): Result<VoiceCommand> {
        Log.d(TAG, "Processing audio bytes: ${audioBytes.size} bytes, type: $mimeType")
        return aiRepository.processAudioCommand(audioBytes, mimeType)
    }
}

/**
 * Real implementation of ExecuteVoiceCommandUseCase
 * Thực thi command và trả về response từ AI (không tự sinh response)
 */
class RealExecuteVoiceCommandUseCase(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) : ExecuteVoiceCommandUseCase {

    companion object {
        private const val TAG = "RealExecuteVoiceCmd"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    override suspend fun invoke(command: VoiceCommand): Result<String> {
        return try {
            Log.d(TAG, "Executing command: ${command.action}")

            when (command.action) {
                // Task operations
                VoiceAction.CREATE_TASK -> {
                    createTask(command)
                    Result.success(command.responseText)
                }
                VoiceAction.DELETE_TASK -> {
                    deleteTask(command)
                    Result.success(command.responseText)
                }
                
                // Mission operations
                VoiceAction.CREATE_MISSION -> {
                    createMission(command)
                    Result.success(command.responseText)
                }
                VoiceAction.DELETE_MISSION -> {
                    deleteMission(command)
                    Result.success(command.responseText)
                }
                VoiceAction.COMPLETE_MISSION -> {
                    completeMission(command)
                    Result.success(command.responseText)
                }
                
                // Query - trả về response từ AI, app sẽ xử lý thêm nếu cần
                VoiceAction.QUERY -> {
                    // Có thể xử lý query ở đây nếu cần
                    // Hiện tại chỉ trả về response từ AI
                    Result.success(command.responseText)
                }
                
                // Chat và Unknown - chỉ trả về response từ AI
                VoiceAction.CHAT, VoiceAction.UNKNOWN -> {
                    Result.success(command.responseText)
                }
                
                // Các action khác chưa implement
                else -> Result.success(command.responseText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            Result.failure(e)
        }
    }

    private suspend fun createTask(command: VoiceCommand) {
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

    private suspend fun deleteTask(command: VoiceCommand) {
        val tasks = taskUseCases.getTasks().first()
        val title = command.params.title ?: throw Exception("Title is required")

        val task = tasks.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Task không tìm thấy: $title")

        taskUseCases.deleteTask(task.id)
        Log.d(TAG, "Task deleted: ${task.title}")
    }

    private suspend fun createMission(command: VoiceCommand) {
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

    private suspend fun deleteMission(command: VoiceCommand) {
        val missions = missionUseCases.getMissions().first()
        val title = command.params.title ?: throw Exception("Title is required")

        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Mission không tìm thấy: $title")

        missionUseCases.deleteMission(mission.id)
        Log.d(TAG, "Mission deleted: ${mission.title}")
    }

    private suspend fun completeMission(command: VoiceCommand) {
        val missions = missionUseCases.getMissions().first()
        val title = command.params.title ?: throw Exception("Title is required")

        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: throw Exception("Mission không tìm thấy: $title")

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
 * Factory function to create AIUseCases for release builds
 */
fun createAIUseCases(
    aiRepository: AiRepository,
    taskUseCases: TaskUseCases,
    missionUseCases: MissionUseCases
): AIUseCases {
    return AIUseCases(
        processVoiceCommand = RealProcessVoiceCommandUseCase(aiRepository),
        processAudioCommand = RealProcessAudioCommandUseCase(aiRepository),
        executeVoiceCommand = RealExecuteVoiceCommandUseCase(taskUseCases, missionUseCases)
    )
}
