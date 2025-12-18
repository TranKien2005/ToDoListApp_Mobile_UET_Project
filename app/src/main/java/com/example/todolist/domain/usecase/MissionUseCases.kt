package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GetMissionsUseCase {
    operator fun invoke(): Flow<List<Mission>>
}

// Split save into create/update
interface CreateMissionUseCase {
    suspend operator fun invoke(mission: Mission): Int  // Return mission ID
}

interface UpdateMissionUseCase {
    suspend operator fun invoke(mission: Mission): Int  // Return mission ID
}

interface DeleteMissionUseCase {
    suspend operator fun invoke(missionId: Int)
}

// Set mission stored status (COMPLETED/UNSPECIFIED only - MISSED is computed)
interface SetMissionStatusUseCase {
    suspend operator fun invoke(missionId: Int, status: MissionStoredStatus)
}

interface GetMissionsByDateUseCase {
    operator fun invoke(date: LocalDate): Flow<List<Mission>>
}

interface GetMissionsByMonthUseCase {
    operator fun invoke(year: Int, month: Int): Flow<List<Mission>>
}

// New: stats support for analysis screen
enum class StatsGranularity {
    DAY,             // Single day - shows missions for that specific day
    DAY_OF_WEEK,     // Week view - shows missions grouped by day of week
    WEEK_OF_MONTH,   // Month view - shows missions grouped by week
    MONTH_OF_YEAR,   // Year view - shows missions grouped by month
    YEAR             // Multi-year view - shows missions grouped by year
}

/**
 * One entry represents a single time unit (day/week/month) with counts for completed/missed/in-progress.
 * label: textual label to show on chart (e.g., Mon, W1, Jan)
 * startDate: the LocalDate representing the start of this period (useful for linking)
 */
data class MissionStatsEntry(
    val label: String,
    val startDate: LocalDate,
    val completed: Int,
    val missed: Int,
    val inProgress: Int
)

interface GetMissionStatsUseCase {
    operator fun invoke(referenceDate: LocalDate, granularity: StatsGranularity): Flow<List<MissionStatsEntry>>
}

data class MissionUseCases(
    val getMissions: GetMissionsUseCase,
    val createMission: CreateMissionUseCase,
    val updateMission: UpdateMissionUseCase,
    val deleteMission: DeleteMissionUseCase,
    val setMissionStatus: SetMissionStatusUseCase,
    val getMissionsByDate: GetMissionsByDateUseCase,
    val getMissionsByMonth: GetMissionsByMonthUseCase,
    val getMissionStats: GetMissionStatsUseCase
)
