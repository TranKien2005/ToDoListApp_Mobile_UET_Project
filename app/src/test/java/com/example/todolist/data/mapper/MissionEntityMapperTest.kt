package com.example.todolist.data.mapper

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.data.local.entity.MissionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class MissionEntityMapperTest {

    @Test
    fun `toDomain maps entity to mission correctly`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 23, 59)
        val epochMillis = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = MissionEntity(
            id = 42,
            title = "Complete Project",
            description = "Finish the project before holiday",
            deadlineEpoch = epochMillis,
            status = "COMPLETED"
        )
        
        val mission = MissionEntityMapper.toDomain(entity)
        
        assertEquals(42, mission.id)
        assertEquals("Complete Project", mission.title)
        assertEquals("Finish the project before holiday", mission.description)
        assertEquals(MissionStoredStatus.COMPLETED, mission.storedStatus)
    }

    @Test
    fun `fromDomain maps mission to entity correctly`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 23, 59)
        
        val mission = Mission(
            id = 42,
            title = "Complete Project",
            description = "Finish the project before holiday",
            deadline = deadline,
            storedStatus = MissionStoredStatus.COMPLETED
        )
        
        val entity = MissionEntityMapper.fromDomain(mission)
        
        assertEquals(42, entity.id)
        assertEquals("Complete Project", entity.title)
        assertEquals("Finish the project before holiday", entity.description)
        assertEquals("COMPLETED", entity.status)
    }

    @Test
    fun `roundtrip mapping preserves all fields`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        
        val original = Mission(
            id = 1,
            title = "Roundtrip Test",
            description = "Testing roundtrip mapping",
            deadline = deadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        val entity = MissionEntityMapper.fromDomain(original)
        val mapped = MissionEntityMapper.toDomain(entity)
        
        assertEquals(original.id, mapped.id)
        assertEquals(original.title, mapped.title)
        assertEquals(original.description, mapped.description)
        assertEquals(original.storedStatus, mapped.storedStatus)
    }

    @Test
    fun `null description is preserved through mapping`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        
        val mission = Mission(
            id = 1,
            title = "No Description Mission",
            description = null,
            deadline = deadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        val entity = MissionEntityMapper.fromDomain(mission)
        val mapped = MissionEntityMapper.toDomain(entity)
        
        assertNull(mapped.description)
    }

    @Test
    fun `invalid status defaults to UNSPECIFIED`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        val epochMillis = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = MissionEntity(
            id = 1,
            title = "Test",
            description = null,
            deadlineEpoch = epochMillis,
            status = "INVALID_STATUS"
        )
        
        val mission = MissionEntityMapper.toDomain(entity)
        
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
    }

    @Test
    fun `UNSPECIFIED status is mapped correctly`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        val epochMillis = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = MissionEntity(
            id = 1,
            title = "Test",
            description = null,
            deadlineEpoch = epochMillis,
            status = "UNSPECIFIED"
        )
        
        val mission = MissionEntityMapper.toDomain(entity)
        
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
    }

    @Test
    fun `COMPLETED status is mapped correctly`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        val epochMillis = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entity = MissionEntity(
            id = 1,
            title = "Test",
            description = null,
            deadlineEpoch = epochMillis,
            status = "COMPLETED"
        )
        
        val mission = MissionEntityMapper.toDomain(entity)
        
        assertEquals(MissionStoredStatus.COMPLETED, mission.storedStatus)
    }

    @Test
    fun `all stored statuses are properly converted in roundtrip`() {
        val deadline = LocalDateTime.of(2024, 12, 25, 12, 0)
        
        MissionStoredStatus.values().forEach { storedStatus ->
            val mission = Mission(
                id = 1,
                title = "Test",
                description = null,
                deadline = deadline,
                storedStatus = storedStatus
            )
            
            val entity = MissionEntityMapper.fromDomain(mission)
            val mapped = MissionEntityMapper.toDomain(entity)
            
            assertEquals(storedStatus, mapped.storedStatus)
        }
    }
}
