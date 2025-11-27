package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.TaskEntity
import com.example.todolist.domain.model.Task
import com.example.todolist.util.extension.DateExt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class TaskMapperTest {
    @Test
    fun `task entity mapping roundtrip`() {
        val now = LocalDateTime.now()
        val task = Task(id = 0, title = "Test", description = "d", startTime = now, endTime = now.plusHours(1))
        val entity = TaskEntityMapper.fromDomain(task)
        val mapped = TaskEntityMapper.toDomain(entity)

        assertEquals(entity.title, mapped.title)
        assertEquals(entity.description, mapped.description)
        assertEquals(entity.isCompleted, mapped.isCompleted)
    }
}

