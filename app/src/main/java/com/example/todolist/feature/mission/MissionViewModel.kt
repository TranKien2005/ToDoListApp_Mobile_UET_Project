package com.example.todolist.feature.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.Mission
import com.example.todolist.domain.usecase.MissionUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class MissionViewModel(
    private val missionUseCases: MissionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState

    // cache of all missions from source so filters can be applied locally
    private val _allMissions = MutableStateFlow<List<Mission>>(emptyList())

    init {
        viewModelScope.launch {
            missionUseCases.getMissions.invoke()
                .collect { list ->
                    _allMissions.value = list
                    val filtered = applyFilters(list, _uiState.value.selectedTag, _uiState.value.statusFilter)
                    _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
                }
        }
    }

    private fun applyFilters(list: List<Mission>, tag: MissionTag, status: MissionStatusFilter): List<Mission> {
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)
        val weekStart = today.minusDays((today.dayOfWeek.ordinal).toLong()) // Sunday-based maybe; acceptable for demo
        val weekEnd = weekStart.plusDays(6)

        return list.filter { mission ->
            val d = mission.deadline.toLocalDate()
            val tagOk = when (tag) {
                MissionTag.ALL -> true
                MissionTag.TODAY -> d.isEqual(today)
                MissionTag.THIS_WEEK -> (d.isEqual(weekStart) || (d.isAfter(weekStart) && d.isBefore(weekEnd))) || d.isEqual(weekEnd)
                MissionTag.THIS_MONTH -> YearMonth.from(d) == thisMonth
            }
            val statusOk = when (status) {
                MissionStatusFilter.ALL -> true
                MissionStatusFilter.COMPLETED -> mission.isCompleted
                MissionStatusFilter.IN_PROGRESS -> !mission.isCompleted && !mission.deadline.toLocalDate().isBefore(today)
                MissionStatusFilter.MISSED -> !mission.isCompleted && mission.deadline.toLocalDate().isBefore(today)
            }
            tagOk && statusOk
        }
    }

    fun selectTag(tag: MissionTag) {
        _uiState.value = _uiState.value.copy(selectedTag = tag, isLoading = true)
        // apply filters on cached list
        val filtered = applyFilters(_allMissions.value, tag, _uiState.value.statusFilter)
        _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
    }

    fun setStatusFilter(filter: MissionStatusFilter) {
        _uiState.value = _uiState.value.copy(statusFilter = filter, isLoading = true)
        val filtered = applyFilters(_allMissions.value, _uiState.value.selectedTag, filter)
        _uiState.value = _uiState.value.copy(missions = filtered, isLoading = false)
    }

    fun deleteMission(id: Int) {
        viewModelScope.launch {
            missionUseCases.deleteMission.invoke(id)
        }
    }

    fun markCompleted(id: Int, completed: Boolean) {
        viewModelScope.launch {
            // find mission by id
            val mission = _allMissions.value.firstOrNull { it.id == id }
            if (mission != null) {
                missionUseCases.completeMission.invoke(mission, completed)
            }
        }
    }
}
