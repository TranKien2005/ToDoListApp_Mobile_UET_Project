package com.example.todolist.di

import com.example.todolist.domain.di.DomainModule

class AppModule (context: android.content.Context) {
    val appContext: android.content.Context = context
    val domainModule = DomainModule(appContext)
}