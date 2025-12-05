package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val _missionsState = MutableStateFlow<List<Mission>>(
    listOf(
        Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.now().plusDays(1), status = MissionStatus.UNSPECIFIED),
        Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.now().plusDays(2), status = MissionStatus.COMPLETED),
        Mission(id = 3, title = "Mission 3", deadline = LocalDateTime.now().plusDays(3), status = MissionStatus.UNSPECIFIED),
        Mission(id = 4, title = "Mission 4", deadline = LocalDateTime.now().minusDays(-1), status = MissionStatus.MISSED),
        Mission(id = 5, title = "Mission 5", deadline = LocalDateTime.now().plusHours(5), status = MissionStatus.UNSPECIFIED),
        Mission(id = 6, title = "Mission 6", deadline = LocalDateTime.now().plusDays(7), status = MissionStatus.COMPLETED)
    )
)

class FakeGetMissionsUseCase: GetMissionsUseCase {
    override operator fun invoke(): Flow<List<Mission>> = _missionsState
}

class FakeCreateMissionUseCase: CreateMissionUseCase {
    override suspend operator fun invoke(mission: Mission) {
        val current = _missionsState.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val created = mission.copy(id = nextId)
        current.add(created)
        _missionsState.value = current
    }
}

class FakeUpdateMissionUseCase: UpdateMissionUseCase {
    override suspend operator fun invoke(mission: Mission) {
        val current = _missionsState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == mission.id }
        if (idx >= 0) current[idx] = mission
        _missionsState.value = current
    }
}

class FakeDeleteMissionUseCase: DeleteMissionUseCase {
    override suspend operator fun invoke(missionId: Int) {
        _missionsState.value = _missionsState.value.filterNot { it.id == missionId }
    }
}

class FakeSetMissionStatusUseCase: SetMissionStatusUseCase {
    override suspend operator fun invoke(missionId: Int, status: MissionStatus) {
        val current = _missionsState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == missionId }
        if (idx >= 0) {
            val updated = current[idx].copy(status = status)
            current[idx] = updated
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

class FakeGetMissionStatsUseCase: GetMissionStatsUseCase {
    override operator fun invoke(referenceDate: LocalDate, granularity: StatsGranularity): Flow<List<MissionStatsEntry>> =
        _missionsState.map { list ->
            when (granularity) {
                StatsGranularity.DAY_OF_WEEK -> {
                    // start from Monday
                    val weekStart = referenceDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    (0..6).map { i ->
                        val day = weekStart.plusDays(i.toLong())
                        val missionsIn = list.filter { it.deadline.toLocalDate().isEqual(day) }
                        val completed = missionsIn.count { it.status == MissionStatus.COMPLETED }
                        val missed = missionsIn.count { it.status == MissionStatus.MISSED }
                        val inProgress = missionsIn.count { it.status != MissionStatus.COMPLETED && it.status != MissionStatus.MISSED }
                        val label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        MissionStatsEntry(label, day, completed, missed, inProgress)
                    }
                }
                StatsGranularity.WEEK_OF_MONTH -> {
                    val ym = YearMonth.from(referenceDate)
                    val daysInMonth = ym.lengthOfMonth()
                    val year = ym.year
                    val month = ym.monthValue
                    val weeks = mutableListOf<MissionStatsEntry>()
                    var startDay = 1
                    var weekIndex = 1
                    while (startDay <= daysInMonth) {
                        val endDay = kotlin.math.min(startDay + 6, daysInMonth)
                        val startDate = LocalDate.of(year, month, startDay)
                        val endDate = LocalDate.of(year, month, endDay)
                        val missionsIn = list.filter { d ->
                            val ld = d.deadline.toLocalDate()
                            (ld.isEqual(startDate) || ld.isAfter(startDate)) && (ld.isEqual(endDate) || ld.isBefore(endDate))
                        }
                        val completed = missionsIn.count { it.status == MissionStatus.COMPLETED }
                        val missed = missionsIn.count { it.status == MissionStatus.MISSED }
                        val inProgress = missionsIn.count { it.status != MissionStatus.COMPLETED && it.status != MissionStatus.MISSED }
                        val label = "W$weekIndex"
                        weeks.add(MissionStatsEntry(label, startDate, completed, missed, inProgress))
                        weekIndex++
                        startDay += 7
                    }
                    weeks
                }
                StatsGranularity.MONTH_OF_YEAR -> {
                    val year = referenceDate.year
                    (1..12).map { m ->
                        val start = LocalDate.of(year, m, 1)
                        val end = start.withDayOfMonth(start.lengthOfMonth())
                        val missionsIn = list.filter { d ->
                            val ld = d.deadline.toLocalDate()
                            (ld.isEqual(start) || ld.isAfter(start)) && (ld.isEqual(end) || ld.isBefore(end))
                        }
                        val completed = missionsIn.count { it.status == MissionStatus.COMPLETED }
                        val missed = missionsIn.count { it.status == MissionStatus.MISSED }
                        val inProgress = missionsIn.count { it.status != MissionStatus.COMPLETED && it.status != MissionStatus.MISSED }
                        val label = start.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        MissionStatsEntry(label, start, completed, missed, inProgress)
                    }
                }
            }
        }
}

val fakeMissionUseCases = MissionUseCases(
    getMissions = FakeGetMissionsUseCase(),
    createMission = FakeCreateMissionUseCase(),
    updateMission = FakeUpdateMissionUseCase(),
    deleteMission = FakeDeleteMissionUseCase(),
    setMissionStatus = FakeSetMissionStatusUseCase(),
    getMissionsByDate = FakeGetMissionByDateUseCase(),
    getMissionsByMonth = FakeGetMissionByMonthUseCase(),
    getMissionStats = FakeGetMissionStatsUseCase()
)
