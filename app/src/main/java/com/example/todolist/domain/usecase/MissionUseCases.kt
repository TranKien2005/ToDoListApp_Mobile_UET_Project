package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GetMissionsUseCase {
    operator fun invoke(): Flow<List<Mission>>
}

interface SaveMissionUseCase {
    suspend operator fun invoke(mission: Mission)
}

interface DeleteMissionUseCase {
    suspend operator fun invoke(missionId: Int)
}

interface CompleteMissionUseCase {
    suspend operator fun invoke(mission: Mission, isCompleted: Boolean)
}

interface GetMissionsByDateUseCase {
    operator fun invoke(date: LocalDate): Flow<List<Mission>>
}

interface GetMissionsByMonthUseCase {
    operator fun invoke(year: Int, month: Int): Flow<List<Mission>>
}

data class MissionUseCases(
    val getMissions: GetMissionsUseCase,
    val saveMission: SaveMissionUseCase,
    val deleteMission: DeleteMissionUseCase,
    val completeMission: CompleteMissionUseCase,
    val getMissionsByDate: GetMissionsByDateUseCase,
    val getMissionsByMonth: GetMissionsByMonthUseCase
)

