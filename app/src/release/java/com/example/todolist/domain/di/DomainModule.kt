package com.example.todolist.domain.di

import android.content.Context
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.data.repository.RoomUserRepositoryImpl
import com.example.todolist.data.repository.RoomSettingsRepositoryImpl
import com.example.todolist.data.repository.RoomTaskRepositoryImpl
import com.example.todolist.data.repository.RoomMissionRepositoryImpl
import com.example.todolist.domain.usecase.*

class DomainModule(context: Context) {
    val appContext = context.applicationContext

    // Database
    private val database = AppDatabase.getInstance(appContext)

    // Repositories
    private val userRepository = RoomUserRepositoryImpl(database.userDao())
    private val settingsRepository = RoomSettingsRepositoryImpl(database.settingsDao())
    private val taskRepository = RoomTaskRepositoryImpl(database.taskDao())
    private val missionRepository = RoomMissionRepositoryImpl(database.missionDao())

    // Use Cases
    val userUseCases = UserUseCases(
        getUser = RealGetUserUseCase(userRepository),
        saveUser = RealSaveUserUseCase(userRepository),
        updateUser = RealUpdateUserUseCase(userRepository),
        deleteUser = RealDeleteUserUseCase(userRepository)
    )

    val settingsUseCases = SettingsUseCases(
        getSettings = RealGetSettingsUseCase(settingsRepository),
        updateSettings = RealUpdateSettingsUseCase(settingsRepository)
    )

    val taskUseCases = TaskUseCases(
        getTasks = RealGetTasksUseCase(taskRepository),
        createTask = RealCreateTaskUseCase(taskRepository),
        updateTask = RealUpdateTaskUseCase(taskRepository),
        deleteTask = RealDeleteTaskUseCase(taskRepository),
        getTasksByDay = RealGetTasksByDayUseCase(taskRepository),
        getTasksByMonth = RealGetTasksByMonthUseCase(taskRepository)
    )

    val missionUseCases = MissionUseCases(
        getMissions = RealGetMissionsUseCase(missionRepository),
        createMission = RealCreateMissionUseCase(missionRepository),
        updateMission = RealUpdateMissionUseCase(missionRepository),
        deleteMission = RealDeleteMissionUseCase(missionRepository),
        getMissionsByDay = RealGetMissionsByDayUseCase(missionRepository),
        getMissionsByMonth = RealGetMissionsByMonthUseCase(missionRepository)
    )
}

