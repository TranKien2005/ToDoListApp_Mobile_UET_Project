package com.example.todolist.util.logger

import android.util.Log

object AppLogger {
    private const val TAG = "Todolist"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}

