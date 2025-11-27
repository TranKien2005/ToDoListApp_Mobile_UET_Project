package com.example.todolist.util.extension

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DateExt {
    fun toLocalDateTime(epochMillis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
    }

    fun toEpochMillis(ldt: LocalDateTime): Long {
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

