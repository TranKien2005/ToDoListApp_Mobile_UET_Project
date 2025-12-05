package com.example.todolist.feature.analysis

import com.example.todolist.domain.usecase.MissionStatsEntry
import com.example.todolist.domain.usecase.StatsGranularity
import java.time.LocalDate

data class AnalysisUiState(
    val stats: List<MissionStatsEntry> = emptyList(),
    val granularity: StatsGranularity = StatsGranularity.WEEK_OF_MONTH,
    val referenceDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true
)

