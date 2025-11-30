package com.example.todolist.domain.repository

import com.example.todolist.core.model.Mission
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun getMissions(): Flow<List<Mission>>
    suspend fun saveMission(mission: Mission)
    suspend fun deleteMission(missionId: Int)
    suspend fun markMissionCompleted(missionId: Int, completed: Boolean)
}

