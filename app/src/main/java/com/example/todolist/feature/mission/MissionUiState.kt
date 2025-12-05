package com.example.todolist.feature.mission

import com.example.todolist.core.model.Mission
import com.example.todolist.domain.usecase.StatsGranularity
import java.time.LocalDate

enum class MissionTag {
    ALL, TODAY, THIS_WEEK, THIS_MONTH
}

enum class MissionStatusFilter {
    ALL, COMPLETED, IN_PROGRESS, MISSED
}

data class MissionUiState(
    val selectedTag: MissionTag = MissionTag.ALL,
    val statusFilter: MissionStatusFilter = MissionStatusFilter.ALL,
    val missions: List<Mission> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val referenceDate: LocalDate = LocalDate.now(),
    val granularity: StatsGranularity = StatsGranularity.WEEK_OF_MONTH
)
