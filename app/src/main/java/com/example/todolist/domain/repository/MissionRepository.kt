package com.example.todolist.domain.repository

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun getMissions(): Flow<List<Mission>>
    suspend fun saveMission(mission: Mission): Int  // Return mission ID
    suspend fun deleteMission(missionId: Int)
    // Set mission stored status (COMPLETED/UNSPECIFIED only)
    suspend fun setMissionStatus(missionId: Int, status: MissionStoredStatus)
}
