package com.example.todolist.domain.di

import android.content.Context
import com.example.todolist.data.remote.ai.GeminiAiRepositoryImpl
import com.example.todolist.domain.usecase.fakeMissionUseCases
import com.example.todolist.domain.usecase.fakeTaskUseCases
import com.example.todolist.domain.usecase.fakeUserUseCases
import com.example.todolist.domain.usecase.fakeSettingsUseCases
import com.example.todolist.domain.usecase.fakeNotificationUseCases
import com.example.todolist.domain.usecase.createFakeAIUseCases

class DomainModule(context: Context) {
    val appContext = context.applicationContext
    val taskUseCases = fakeTaskUseCases
    val missionUseCases = fakeMissionUseCases
    val userUseCases = fakeUserUseCases
    val settingsUseCases = fakeSettingsUseCases
    val notificationUseCases = fakeNotificationUseCases

    // AI Use Cases - Fake implementation for debug
    val aiUseCases = createFakeAIUseCases()
}