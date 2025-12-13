package com.example.todolist.data.di

import android.content.Context
import com.example.todolist.data.local.dao.MissionDao
import com.example.todolist.data.local.dao.NotificationDao
import com.example.todolist.data.local.dao.SettingsDao
import com.example.todolist.data.local.dao.TaskDao
import com.example.todolist.data.local.di.LocalModule
import com.example.todolist.data.repository.RoomMissionRepositoryImpl
import com.example.todolist.data.repository.RoomNotificationRepositoryImpl
import com.example.todolist.data.repository.RoomSettingsRepositoryImpl
import com.example.todolist.data.repository.RoomTaskRepositoryImpl
import com.example.todolist.domain.repository.MissionRepository
import com.example.todolist.domain.repository.NotificationRepository
import com.example.todolist.domain.repository.SettingsRepository
import com.example.todolist.domain.repository.TaskRepository

object RepositoryModule {
    /** Provide MissionRepository from MissionDao */
    fun provideMissionRepository(dao: MissionDao): MissionRepository = RoomMissionRepositoryImpl(dao)

    fun provideMissionRepository(context: Context): MissionRepository = provideMissionRepository(LocalModule.provideMissionDao(context))

    /** Provide TaskRepository from TaskDao */
    fun provideTaskRepository(dao: TaskDao): TaskRepository = RoomTaskRepositoryImpl(dao)

    fun provideTaskRepository(context: Context): TaskRepository = provideTaskRepository(LocalModule.provideTaskDao(context))

    /** Provide NotificationRepository from NotificationDao */
    fun provideNotificationRepository(dao: NotificationDao): NotificationRepository = RoomNotificationRepositoryImpl(dao)

    fun provideNotificationRepository(context: Context): NotificationRepository = provideNotificationRepository(LocalModule.provideNotificationDao(context))

    /** Provide SettingsRepository from SettingsDao */
    fun provideSettingsRepository(dao: SettingsDao): SettingsRepository = RoomSettingsRepositoryImpl(dao)

    fun provideSettingsRepository(context: Context): SettingsRepository = provideSettingsRepository(LocalModule.provideSettingsDao(context))

    /** Provide AiRepository using AiApiService (remote). If you later add local persistence for AI results, change this to compose local+remote */
}
