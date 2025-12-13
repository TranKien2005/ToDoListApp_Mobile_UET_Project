package com.example.todolist.domain.di

import androidx.credentials.CredentialManager
import com.example.todolist.BuildConfig
import com.example.todolist.data.repository.GoogleAuthRepository
import com.example.todolist.data.repository.GoogleAuthRepositoryImpl
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.data.repository.GoogleCalendarRepositoryImpl
import com.example.todolist.domain.usecase.fakeMissionUseCases
import com.example.todolist.domain.usecase.fakeTaskUseCases
import com.example.todolist.domain.usecase.fakeTaskRepository
import com.example.todolist.domain.usecase.fakeUserUseCases
import com.example.todolist.domain.usecase.fakeSettingsUseCases
import com.example.todolist.domain.usecase.fakeNotificationUseCases
import com.example.todolist.domain.usecase.createAIUseCases

class DomainModule(context: android.content.Context) {
    val appContext = context.applicationContext
    
    // Use Cases
    val taskUseCases = fakeTaskUseCases
    val missionUseCases = fakeMissionUseCases
    val userUseCases = fakeUserUseCases
    val settingsUseCases = fakeSettingsUseCases
    val notificationUseCases = fakeNotificationUseCases

    // Task Repository (for Calendar sync)
    val taskRepository = fakeTaskRepository

    // Google Auth Repository
    val googleAuthRepository: GoogleAuthRepository = GoogleAuthRepositoryImpl(
        credentialManager = CredentialManager.create(appContext),
        webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    )

    // Google Calendar Repository
    val googleCalendarRepository: GoogleCalendarRepository = GoogleCalendarRepositoryImpl()

    // AI Use Cases - Mock implementation for debug
    val aiUseCases = createAIUseCases(appContext, taskUseCases, missionUseCases)
    
    // Legacy alias for backward compatibility
    val googleAuthUseCases = googleAuthRepository
}