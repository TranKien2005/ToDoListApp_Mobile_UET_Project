package com.example.todolist.feature.mission

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.domain.usecase.NotificationUseCases
import com.example.todolist.domain.usecase.StatsGranularity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

class MissionViewModel(
    private val missionUseCases: MissionUseCases,
    private val notificationUseCases: NotificationUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState

    // cache of all missions from source so filters can be applied locally
    private val _allMissions = MutableStateFlow<List<Mission>>(emptyList())

    // reference date and granularity for navigation
    private val _referenceDate = MutableStateFlow(LocalDate.now())
    private val _granularity = MutableStateFlow(StatsGranularity.DAY)

    init {
        // initialize base state
        _uiState.value = _uiState.value.copy(referenceDate = _referenceDate.value, granularity = _granularity.value, isLoading = true)

        viewModelScope.launch {
            missionUseCases.getMissions.invoke()
                .collect { list ->
                    _allMissions.value = list
                    val filtered = applyFilters(list, _uiState.value.selectedTag, _uiState.value.statusFilter, _referenceDate.value, _granularity.value)
                    Log.d("MissionViewModel", "init collected=${list.size} filtered=${filtered.size} ref=${_referenceDate.value} gran=${_granularity.value}")
                    _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
                }
        }
    }

    private fun missionIsCompleted(mission: Mission): Boolean = mission.status == MissionStatus.COMPLETED
    private fun missionIsMissed(mission: Mission): Boolean = mission.status == MissionStatus.MISSED

    private fun applyFilters(list: List<Mission>, tag: MissionTag, status: MissionStatusFilter, refDate: LocalDate, gran: StatsGranularity): List<Mission> {
        val today = refDate
        val thisMonth = YearMonth.from(today)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)

        return list.filter { mission ->
            val d = mission.deadline.toLocalDate()
            val tagOk = when (tag) {
                MissionTag.ALL -> {
                    when (gran) {
                        StatsGranularity.DAY -> d.isEqual(today)
                        StatsGranularity.DAY_OF_WEEK -> (d.isEqual(weekStart) || (d.isAfter(weekStart) && d.isBefore(weekEnd))) || d.isEqual(weekEnd)
                        StatsGranularity.WEEK_OF_MONTH -> YearMonth.from(d) == thisMonth
                        StatsGranularity.MONTH_OF_YEAR -> d.year == today.year
                        StatsGranularity.YEAR -> true // Show all missions for multi-year view
                    }
                }
                MissionTag.TODAY -> d.isEqual(today)
                MissionTag.THIS_WEEK -> (d.isEqual(weekStart) || (d.isAfter(weekStart) && d.isBefore(weekEnd))) || d.isEqual(weekEnd)
                MissionTag.THIS_MONTH -> YearMonth.from(d) == thisMonth
            }
            val statusOk = when (status) {
                MissionStatusFilter.ALL -> true
                MissionStatusFilter.COMPLETED -> missionIsCompleted(mission)
                MissionStatusFilter.IN_PROGRESS -> !missionIsCompleted(mission) && !missionIsMissed(mission)
                MissionStatusFilter.MISSED -> missionIsMissed(mission)
            }
            tagOk && statusOk
        }
    }

    fun selectTag(tag: MissionTag) {
        _uiState.value = _uiState.value.copy(selectedTag = tag, isLoading = true)
        val filtered = applyFilters(_allMissions.value, tag, _uiState.value.statusFilter, _referenceDate.value, _granularity.value)
        Log.d("MissionViewModel", "selectTag=$tag filtered=${filtered.size} ref=${_referenceDate.value} gran=${_granularity.value}")
        _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
    }

    fun setStatusFilter(filter: MissionStatusFilter) {
        _uiState.value = _uiState.value.copy(statusFilter = filter, isLoading = true)
        val filtered = applyFilters(_allMissions.value, _uiState.value.selectedTag, filter, _referenceDate.value, _granularity.value)
        Log.d("MissionViewModel", "setStatusFilter=$filter filtered=${filtered.size} ref=${_referenceDate.value} gran=${_granularity.value}")
        _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
    }

    fun setGranularity(g: StatsGranularity) {
        _granularity.value = g
        // recompute missions for the current reference date using the new granularity
        val filtered = applyFilters(_allMissions.value, _uiState.value.selectedTag, _uiState.value.statusFilter, _referenceDate.value, g)
        Log.d("MissionViewModel", "setGranularity=$g filtered=${filtered.size} ref=${_referenceDate.value}")
        _uiState.value = _uiState.value.copy(granularity = g, missions = filtered)
    }

    fun prev() {
        val ref = _referenceDate.value
        val newRef = when (_granularity.value) {
            StatsGranularity.DAY -> ref.minusDays(1)
            StatsGranularity.DAY_OF_WEEK -> ref.minusWeeks(1)
            StatsGranularity.WEEK_OF_MONTH -> ref.minusMonths(1)
            StatsGranularity.MONTH_OF_YEAR -> ref.minusYears(1)
            StatsGranularity.YEAR -> ref.minusYears(5)
        }
        val filtered = applyFilters(_allMissions.value, _uiState.value.selectedTag, _uiState.value.statusFilter, newRef, _granularity.value)
        Log.d("MissionViewModel", "prev newRef=$newRef filtered=${filtered.size} gran=${_granularity.value}")
        _referenceDate.value = newRef
        _uiState.value = _uiState.value.copy(referenceDate = newRef, missions = filtered, isLoading = false)
    }

    fun next() {
        val ref = _referenceDate.value
        val newRef = when (_granularity.value) {
            StatsGranularity.DAY -> ref.plusDays(1)
            StatsGranularity.DAY_OF_WEEK -> ref.plusWeeks(1)
            StatsGranularity.WEEK_OF_MONTH -> ref.plusMonths(1)
            StatsGranularity.MONTH_OF_YEAR -> ref.plusYears(1)
            StatsGranularity.YEAR -> ref.plusYears(5)
        }
        val filtered = applyFilters(_allMissions.value, _uiState.value.selectedTag, _uiState.value.statusFilter, newRef, _granularity.value)
        Log.d("MissionViewModel", "next newRef=$newRef filtered=${filtered.size} gran=${_granularity.value}")
        _referenceDate.value = newRef
        _uiState.value = _uiState.value.copy(referenceDate = newRef, missions = filtered, isLoading = false)
    }

    fun deleteMission(id: Int) {
        viewModelScope.launch {
            missionUseCases.deleteMission.invoke(id)
        }
    }

    // Toggle or set mission status. If mission currently COMPLETED, set to UNSPECIFIED; otherwise set to COMPLETED.
    fun toggleMissionCompleted(id: Int) {
        viewModelScope.launch {
            val mission = _allMissions.value.firstOrNull { it.id == id }
            if (mission != null) {
                // Do not allow toggling for MISSED missions (they should only be deletable)
                if (mission.status == MissionStatus.MISSED) return@launch

                val newStoredStatus = if (mission.storedStatus == MissionStoredStatus.COMPLETED) {
                    MissionStoredStatus.UNSPECIFIED
                } else {
                    MissionStoredStatus.COMPLETED
                }
                missionUseCases.setMissionStatus.invoke(id, newStoredStatus)

                // If marking as COMPLETED, cancel all notifications for this mission
                if (newStoredStatus == MissionStoredStatus.COMPLETED) {
                    notificationUseCases.cancelMissionNotifications.invoke(id)
                }
            }
        }
    }

    // New: save a mission
    fun saveMission(mission: Mission) {
        viewModelScope.launch {
            try {
                if (mission.id == 0) {
                    missionUseCases.createMission.invoke(mission)
                } else {
                    missionUseCases.updateMission.invoke(mission)
                }
            } catch (_: Throwable) {
                // ignore for now; in full app you'd update UI state with error
            }
        }
    }
}
