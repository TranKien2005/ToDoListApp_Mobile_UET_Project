package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// Release implementations that use real MissionRepository
class RealGetMissionsUseCase(
    private val repository: MissionRepository
) : GetMissionsUseCase {
    override operator fun invoke(): Flow<List<Mission>> = repository.getMissions()
}

class RealCreateMissionUseCase(
    private val repository: MissionRepository
) : CreateMissionUseCase {
    override suspend operator fun invoke(mission: Mission) {
        repository.saveMission(mission)
    }
}

class RealUpdateMissionUseCase(
    private val repository: MissionRepository
) : UpdateMissionUseCase {
    override suspend operator fun invoke(mission: Mission) {
        repository.saveMission(mission)
    }
}

class RealDeleteMissionUseCase(
    private val repository: MissionRepository
) : DeleteMissionUseCase {
    override suspend operator fun invoke(missionId: Int) {
        repository.deleteMission(missionId)
    }
}

class RealSetMissionStatusUseCase(
    private val repository: MissionRepository
) : SetMissionStatusUseCase {
    override suspend operator fun invoke(missionId: Int, status: MissionStatus) {
        repository.setMissionStatus(missionId, status)
    }
}

class RealGetMissionsByDateUseCase(
    private val repository: MissionRepository
) : GetMissionsByDateUseCase {
    override operator fun invoke(date: LocalDate): Flow<List<Mission>> =
        repository.getMissions().map { list ->
            list.filter { mission ->
                mission.deadline.toLocalDate() == date
            }
        }
}

class RealGetMissionsByMonthUseCase(
    private val repository: MissionRepository
) : GetMissionsByMonthUseCase {
    override operator fun invoke(year: Int, month: Int): Flow<List<Mission>> =
        repository.getMissions().map { list ->
            list.filter { mission ->
                val deadline = mission.deadline
                deadline.year == year && deadline.monthValue == month
            }
        }
}

class RealGetMissionStatsUseCase(
    private val repository: MissionRepository
) : GetMissionStatsUseCase {
    override operator fun invoke(referenceDate: LocalDate, granularity: StatsGranularity): Flow<List<MissionStatsEntry>> =
        repository.getMissions().map { list ->
            when (granularity) {
                StatsGranularity.DAY_OF_WEEK -> {
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

