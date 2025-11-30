package com.example.todolist.domain.di

import com.example.todolist.domain.usecase.fakeMissionUseCases
import com.example.todolist.domain.usecase.fakeTaskUseCases

class DomainModule (context: android.content.Context) {
    val appContext = context.applicationContext
    val taskUseCases = fakeTaskUseCases

    val missionUseCases = fakeMissionUseCases
}