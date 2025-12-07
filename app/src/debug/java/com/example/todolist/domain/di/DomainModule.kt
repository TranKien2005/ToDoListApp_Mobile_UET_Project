package com.example.todolist.domain.di

import com.example.todolist.domain.usecase.fakeMissionUseCases
import com.example.todolist.domain.usecase.fakeTaskUseCases
import com.example.todolist.domain.usecase.fakeUserUseCases
import com.example.todolist.domain.usecase.fakeSettingsUseCases
import com.example.todolist.domain.usecase.fakeNotificationUseCases
import com.example.todolist.domain.usecase.createAIUseCases

class DomainModule (context: android.content.Context) {
    val appContext = context.applicationContext
    val taskUseCases = fakeTaskUseCases
    val missionUseCases = fakeMissionUseCases
    val userUseCases = fakeUserUseCases
    val settingsUseCases = fakeSettingsUseCases
    val notificationUseCases = fakeNotificationUseCases

    // AI Use Cases - Mock implementation for debug
    val aiUseCases = createAIUseCases(appContext, taskUseCases, missionUseCases)
}