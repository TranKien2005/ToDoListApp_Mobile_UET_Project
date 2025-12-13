package com.example.todolist.data.mapper

import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.data.local.entity.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TaskEntityMapperTest {

    @Test
    fun `toDomain maps entity to task correctly`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        val epochMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = TaskEntity(
            id = 42,
            title = "Test Task",
            description = "Description",
            startTimeEpoch = epochMillis,
            durationMinutes = 90L,
            repeatType = "WEEKLY"
        )
        
        val task = TaskEntityMapper.toDomain(entity)
        
        assertEquals(42, task.id)
        assertEquals("Test Task", task.title)
        assertEquals("Description", task.description)
        assertEquals(90L, task.durationMinutes)
        assertEquals(RepeatType.WEEKLY, task.repeatType)
    }

    @Test
    fun `fromDomain maps task to entity correctly`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        
        val task = Task(
            id = 42,
            title = "Test Task",
            description = "Description",
            startTime = now,
            durationMinutes = 90L,
            repeatType = RepeatType.MONTHLY
        )
        
        val entity = TaskEntityMapper.fromDomain(task)
        
        assertEquals(42, entity.id)
        assertEquals("Test Task", entity.title)
        assertEquals("Description", entity.description)
        assertEquals(90L, entity.durationMinutes)
        assertEquals("MONTHLY", entity.repeatType)
    }

    @Test
    fun `roundtrip mapping preserves all fields`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        
        val original = Task(
            id = 1,
            title = "Roundtrip Test",
            description = "Testing roundtrip",
            startTime = now,
            durationMinutes = 45L,
            repeatType = RepeatType.DAILY
        )
        
        val entity = TaskEntityMapper.fromDomain(original)
        val mapped = TaskEntityMapper.toDomain(entity)
        
        assertEquals(original.id, mapped.id)
        assertEquals(original.title, mapped.title)
        assertEquals(original.description, mapped.description)
        assertEquals(original.durationMinutes, mapped.durationMinutes)
        assertEquals(original.repeatType, mapped.repeatType)
    }

    @Test
    fun `null description is preserved through mapping`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        
        val task = Task(
            id = 1,
            title = "No Description",
            description = null,
            startTime = now,
            durationMinutes = null,
            repeatType = RepeatType.NONE
        )
        
        val entity = TaskEntityMapper.fromDomain(task)
        val mapped = TaskEntityMapper.toDomain(entity)
        
        assertNull(mapped.description)
        assertNull(mapped.durationMinutes)
    }

    @Test
    fun `invalid repeat type defaults to NONE`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        val epochMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = TaskEntity(
            id = 1,
            title = "Test",
            description = null,
            startTimeEpoch = epochMillis,
            durationMinutes = null,
            repeatType = "INVALID_TYPE"
        )
        
        val task = TaskEntityMapper.toDomain(entity)
        
        assertEquals(RepeatType.NONE, task.repeatType)
    }

    @Test
    fun `all repeat types are properly converted`() {
        val now = LocalDateTime.of(2024, 6, 15, 10, 30)
        val epochMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        RepeatType.values().forEach { repeatType ->
            val entity = TaskEntity(
                id = 1,
                title = "Test",
                description = null,
                startTimeEpoch = epochMillis,
                durationMinutes = null,
                repeatType = repeatType.name
            )
            
            val task = TaskEntityMapper.toDomain(entity)
            assertEquals(repeatType, task.repeatType)
        }
    }
}
