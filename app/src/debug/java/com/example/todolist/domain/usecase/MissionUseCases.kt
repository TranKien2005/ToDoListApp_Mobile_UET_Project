package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

private val _missionsState = MutableStateFlow<List<Mission>>(
    listOf(
        Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.now().plusDays(1)),
        Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.now().plusDays(2), isCompleted = true),
        Mission(id = 3, title = "Mission 3", deadline = LocalDateTime.now().plusDays(3)),
        Mission(id = 4, title = "Mission 4", deadline = LocalDateTime.now().minusDays(1)),
        Mission(id = 5, title = "Mission 5", deadline = LocalDateTime.now().plusHours(5)),
        Mission(id = 6, title = "Mission 6", deadline = LocalDateTime.now().plusDays(7), isCompleted = true)
    )
)

class FakeGetMissionsUseCase: GetMissionsUseCase {
    override operator fun invoke(): Flow<List<Mission>> = _missionsState
}

class FakeSaveMissionUseCase: SaveMissionUseCase {
    override suspend operator fun invoke(mission: Mission) {
        val current = _missionsState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == mission.id }
        if (idx >= 0) current[idx] = mission else current.add(mission)
        _missionsState.value = current
    }
}

class FakeDeleteMissionUseCase: DeleteMissionUseCase {
    override suspend operator fun invoke(missionId: Int) {
        _missionsState.value = _missionsState.value.filterNot { it.id == missionId }
    }
}

class FakeCompleteMissionUseCase: CompleteMissionUseCase {
    override suspend operator fun invoke(mission: Mission, isCompleted: Boolean) {
        val current = _missionsState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == mission.id }
        if (idx >= 0) {
            val updatedMission = mission.copy(isCompleted = isCompleted)
            current[idx] = updatedMission
            _missionsState.value = current
        }
    }
}

class FakeGetMissionByDateUseCase: GetMissionsByDateUseCase {
    override operator fun invoke(date: LocalDate): Flow<List<Mission>> =
        _missionsState.map { list ->
            list.filter { mission ->
                mission.deadline.toLocalDate() == date
            }
        }
}

class FakeGetMissionByMonthUseCase: GetMissionsByMonthUseCase {
    override operator fun invoke(year: Int, month: Int): Flow<List<Mission>> =
        _missionsState.map { list ->
            list.filter { mission ->
                val deadline = mission.deadline
                deadline.year == year && deadline.monthValue == month
            }
        }
}

val fakeMissionUseCases = MissionUseCases(
    getMissions = FakeGetMissionsUseCase(),
    saveMission = FakeSaveMissionUseCase(),
    deleteMission = FakeDeleteMissionUseCase(),
    completeMission = FakeCompleteMissionUseCase(),
    getMissionsByDate = FakeGetMissionByDateUseCase(),
    getMissionsByMonth = FakeGetMissionByMonthUseCase()
)




