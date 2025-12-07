package com.example.todolist.domain.ai

import android.util.Log
import com.example.todolist.domain.ai.models.VoiceAction
import com.example.todolist.domain.ai.models.VoiceCommand
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.domain.usecase.TaskUseCases
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Execute voice commands - chuyển đổi VoiceCommand thành Task/Mission actions
 * Đây là helper class được sử dụng bởi ExecuteVoiceCommandUseCase
 */
class VoiceCommandExecutor(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) {
    companion object {
        private const val TAG = "VoiceCommandExecutor"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    /**
     * Execute voice command và trả về response message
     */
    suspend fun execute(command: VoiceCommand): Result<String> {
        return try {
            Log.d(TAG, "Executing command: ${command.action}")

            when (command.action) {
                VoiceAction.CREATE_TASK -> createTask(command)
                VoiceAction.CREATE_MISSION -> createMission(command)
                VoiceAction.LIST_TASKS -> listTasks(command)
                VoiceAction.LIST_MISSIONS -> listMissions(command)
                VoiceAction.COMPLETE_TASK -> completeTask(command)
                VoiceAction.COMPLETE_MISSION -> completeMission(command)
                VoiceAction.DELETE_TASK -> deleteTask(command)
                VoiceAction.DELETE_MISSION -> deleteMission(command)
                VoiceAction.UNKNOWN -> Result.success(command.responseText)
                else -> Result.success("Chức năng này chưa được hỗ trợ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            Result.failure(e)
        }
    }

    /**
     * Tạo task mới
     */
    private suspend fun createTask(command: VoiceCommand): Result<String> {
        val params = command.params

        // Parse date
        val date = params.date?.let { parseDate(it) } ?: LocalDate.now()

        // Parse time
        val time = params.time?.let { parseTime(it) } ?: LocalTime.of(9, 0)

        // Duration (default 60 minutes) - must be Long
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
        return Result.success(command.responseText)
    }

    /**
     * Tạo mission mới
     */
    private suspend fun createMission(command: VoiceCommand): Result<String> {
        val params = command.params

        // Parse deadline date
        val date = params.date?.let { parseDate(it) } ?: LocalDate.now().plusDays(7)

        // Parse deadline time (default end of day)
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
        return Result.success(command.responseText)
    }

    /**
     * List tasks
     */
    private suspend fun listTasks(command: VoiceCommand): Result<String> {
        val tasks = taskUseCases.getTasks().first()

        if (tasks.isEmpty()) {
            return Result.success("Bạn chưa có task nào.")
        }

        val taskList = tasks.take(5).joinToString("\n") { task ->
            "- ${task.title} (${task.startTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))})"
        }

        val response = "Bạn có ${tasks.size} tasks. Dưới đây là 5 tasks gần nhất:\n$taskList"
        return Result.success(response)
    }

    /**
     * List missions
     */
    private suspend fun listMissions(command: VoiceCommand): Result<String> {
        val missions = missionUseCases.getMissions().first()

        if (missions.isEmpty()) {
            return Result.success("Bạn chưa có mission nào.")
        }

        val missionList = missions.take(5).joinToString("\n") { mission ->
            "- ${mission.title} (${mission.status.name}, deadline: ${mission.deadline.format(DateTimeFormatter.ofPattern("dd/MM"))})"
        }

        val response = "Bạn có ${missions.size} missions. Dưới đây là 5 missions gần nhất:\n$missionList"
        return Result.success(response)
    }

    /**
     * Complete task
     */
    private suspend fun completeTask(command: VoiceCommand): Result<String> {
        // Task không có completion state - không thể complete task
        return Result.failure(Exception("Tasks không có completion state. Chỉ Missions mới có thể complete."))
    }

    /**
     * Complete mission
     */
    private suspend fun completeMission(command: VoiceCommand): Result<String> {
        val missions = missionUseCases.getMissions().first()
        val title = command.params.title ?: return Result.failure(Exception("Title is required"))

        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: return Result.failure(Exception("Mission không tìm thấy: $title"))

        missionUseCases.setMissionStatus(mission.id, MissionStoredStatus.COMPLETED)

        Log.d(TAG, "Mission completed: ${mission.title}")
        return Result.success(command.responseText)
    }

    /**
     * Delete task
     */
    private suspend fun deleteTask(command: VoiceCommand): Result<String> {
        val tasks = taskUseCases.getTasks().first()
        val title = command.params.title ?: return Result.failure(Exception("Title is required"))

        val task = tasks.find { it.title.contains(title, ignoreCase = true) }
            ?: return Result.failure(Exception("Task không tìm thấy: $title"))

        taskUseCases.deleteTask(task.id)

        Log.d(TAG, "Task deleted: ${task.title}")
        return Result.success(command.responseText)
    }

    /**
     * Delete mission
     */
    private suspend fun deleteMission(command: VoiceCommand): Result<String> {
        val missions = missionUseCases.getMissions().first()
        val title = command.params.title ?: return Result.failure(Exception("Title is required"))

        val mission = missions.find { it.title.contains(title, ignoreCase = true) }
            ?: return Result.failure(Exception("Mission không tìm thấy: $title"))

        missionUseCases.deleteMission(mission.id)

        Log.d(TAG, "Mission deleted: ${mission.title}")
        return Result.success(command.responseText)
    }

    /**
     * Parse date string (dd/MM/yyyy) to LocalDate
     */
    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateStr, using today")
            LocalDate.now()
        }
    }

    /**
     * Parse time string (HH:mm) to LocalTime
     */
    private fun parseTime(timeStr: String): LocalTime {
        return try {
            LocalTime.parse(timeStr, TIME_FORMATTER)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse time: $timeStr, using 09:00")
            LocalTime.of(9, 0)
        }
    }
}
