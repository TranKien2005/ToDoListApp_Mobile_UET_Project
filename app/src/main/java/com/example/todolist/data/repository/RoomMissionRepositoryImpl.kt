package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.MissionDao
import com.example.todolist.data.mapper.MissionEntityMapper
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomMissionRepositoryImpl(
    private val dao: MissionDao
) : MissionRepository {
    override fun getMissions(): Flow<List<Mission>> =
        dao.getAll()
            .map { list -> list.map { MissionEntityMapper.toDomain(it) } }
            .distinctUntilChanged()

    override suspend fun saveMission(mission: Mission): Int {
        val entity = MissionEntityMapper.fromDomain(mission)
        val id = dao.insert(entity)
        return id.toInt()
    }

    override suspend fun deleteMission(missionId: Int) {
        dao.deleteById(missionId)
    }

    override suspend fun setMissionStatus(missionId: Int, status: MissionStoredStatus) {
        dao.updateStatus(missionId, status.name)
    }
}
