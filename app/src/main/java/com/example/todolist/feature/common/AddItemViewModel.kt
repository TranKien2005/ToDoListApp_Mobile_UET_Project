package com.example.todolist.feature.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.usecase.TaskUseCases
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission
import kotlinx.coroutines.launch

/**
 * Dedicated ViewModel for the AddItem bottom sheet.
 * Receives both Task and Mission use cases so it can save either type.
 */
class AddItemViewModel(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) : ViewModel() {

    // Keep a single public save API for UI; ViewModel decides create vs update depending on id.
    fun saveTask(task: Task) {
        viewModelScope.launch {
            try {
                if (task.id == 0) {
                    taskUseCases.createTask.invoke(task)
                } else {
                    taskUseCases.updateTask.invoke(task)
                }
            } catch (_: Throwable) {
                // ignore for now; callers can refresh lists from their ViewModels
            }
        }
    }

    fun saveMission(mission: Mission) {
        viewModelScope.launch {
            try {
                if (mission.id == 0) {
                    missionUseCases.createMission.invoke(mission)
                } else {
                    missionUseCases.updateMission.invoke(mission)
                }
            } catch (_: Throwable) {
                // ignore
            }
        }
    }
}
