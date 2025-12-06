package com.example.todolist.feature.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.usecase.TaskUseCases
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.domain.usecase.NotificationUseCases
import com.example.todolist.domain.usecase.SettingsUseCases
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Dedicated ViewModel for the AddItem bottom sheet.
 * Receives both Task and Mission use cases so it can save either type.
 */
class AddItemViewModel(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases,
    private val notificationUseCases: NotificationUseCases,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    // Keep a single public save API for UI; ViewModel decides create vs update depending on id.
    fun saveTask(task: Task) {
        viewModelScope.launch {
            try {
                // Save task to database first
                if (task.id == 0) {
                    taskUseCases.createTask.invoke(task)
                } else {
                    taskUseCases.updateTask.invoke(task)
                    // Cancel old notifications when updating
                    notificationUseCases.cancelTaskNotifications.invoke(task.id)
                }

                // Lên lịch thông báo cho task
                // Note: We need to get the task from DB to have the correct ID
                val settings = settingsUseCases.getSettings.invoke().first()
                notificationUseCases.scheduleTaskNotification.invoke(task, settings.taskReminderMinutes)
            } catch (e: Throwable) {
                e.printStackTrace() // Log error để debug
            }
        }
    }

    fun saveMission(mission: Mission) {
        viewModelScope.launch {
            try {
                // Save mission to database first
                if (mission.id == 0) {
                    missionUseCases.createMission.invoke(mission)
                } else {
                    missionUseCases.updateMission.invoke(mission)
                    // Cancel old notifications when updating
                    notificationUseCases.cancelMissionNotifications.invoke(mission.id)
                }

                // Lên lịch thông báo cho mission
                val settings = settingsUseCases.getSettings.invoke().first()
                notificationUseCases.scheduleMissionNotification.invoke(mission, settings.missionDeadlineWarningMinutes)
            } catch (e: Throwable) {
                e.printStackTrace() // Log error để debug
            }
        }
    }
}
