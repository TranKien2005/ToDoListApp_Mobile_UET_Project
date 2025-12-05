package com.example.todolist.domain.repository

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun getMissions(): Flow<List<Mission>>
    suspend fun saveMission(mission: Mission)
    suspend fun deleteMission(missionId: Int)
    // set stored mission status (COMPLETED/UNSPECIFIED). MISSED is derived.
    suspend fun setMissionStatus(missionId: Int, status: MissionStatus)
}
