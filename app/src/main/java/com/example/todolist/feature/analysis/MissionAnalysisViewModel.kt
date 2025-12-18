package com.example.todolist.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.usecase.GetMissionStatsUseCase
import com.example.todolist.domain.usecase.StatsGranularity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("unused")
class MissionAnalysisViewModel(
    private val getMissionStatsUseCase: GetMissionStatsUseCase
) : ViewModel() {

    private val _referenceDate = MutableStateFlow(LocalDate.now())
    private val _granularity = MutableStateFlow(StatsGranularity.WEEK_OF_MONTH)

    private val _uiState = MutableStateFlow(AnalysisUiState(isLoading = true))
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        // initialize base state
        _uiState.value = _uiState.value.copy(referenceDate = _referenceDate.value, granularity = _granularity.value, isLoading = true)

        // whenever referenceDate or granularity changes, fetch new stats and update uiState
        viewModelScope.launch {
            _referenceDate
                .flatMapLatest { ref ->
                    _granularity.flatMapLatest { gran ->
                        // set loading true while fetching
                        _uiState.value = _uiState.value.copy(isLoading = true, referenceDate = ref, granularity = gran)
                        getMissionStatsUseCase.invoke(ref, gran)
                    }
                }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(stats = list, isLoading = false)
                }
        }
    }

    fun setGranularity(g: StatsGranularity) {
        _granularity.value = g
    }

    fun prev() {
        val ref = _referenceDate.value
        val newRef = when (_granularity.value) {
            StatsGranularity.DAY -> ref.minusDays(1)
            StatsGranularity.DAY_OF_WEEK -> ref.minusWeeks(1)
            StatsGranularity.WEEK_OF_MONTH -> ref.minusMonths(1)
            StatsGranularity.MONTH_OF_YEAR -> ref.minusYears(1)
        }
        _referenceDate.value = newRef
    }

    fun next() {
        val ref = _referenceDate.value
        val newRef = when (_granularity.value) {
            StatsGranularity.DAY -> ref.plusDays(1)
            StatsGranularity.DAY_OF_WEEK -> ref.plusWeeks(1)
            StatsGranularity.WEEK_OF_MONTH -> ref.plusMonths(1)
            StatsGranularity.MONTH_OF_YEAR -> ref.plusYears(1)
        }
        _referenceDate.value = newRef
    }

    // convenience totals computed from current uiState
    fun totalCompleted(): Int = _uiState.value.stats.sumOf { it.completed }
    fun totalMissed(): Int = _uiState.value.stats.sumOf { it.missed }
    fun totalInProgress(): Int = _uiState.value.stats.sumOf { it.inProgress }
    fun totalMissions(): Int = totalCompleted() + totalMissed() + totalInProgress()
}
