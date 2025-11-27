package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.MissionDao
import com.example.todolist.data.mapper.MissionEntityMapper
import com.example.todolist.domain.model.Mission
import com.example.todolist.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMissionRepositoryImpl(
    private val dao: MissionDao
) : MissionRepository {
    override fun getMissions(): Flow<List<Mission>> = dao.getAll().map { list -> list.map { MissionEntityMapper.toDomain(it) } }

    override suspend fun saveMission(mission: Mission) {
        dao.insert(MissionEntityMapper.fromDomain(mission))
    }

    override suspend fun deleteMission(missionId: Int) {
        dao.deleteById(missionId)
    }

    override suspend fun markMissionCompleted(missionId: Int, completed: Boolean) {
        dao.updateCompleted(missionId, completed)
    }
}

