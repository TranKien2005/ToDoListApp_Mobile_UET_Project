package com.example.todolist.util.extension

fun String?.orEmptyTrimmed(): String = this?.trim() ?: ""

