package com.example.todolist.feature.analysis

import com.example.todolist.domain.usecase.GetMissionStatsUseCase
import com.example.todolist.domain.usecase.MissionStatsEntry
import com.example.todolist.domain.usecase.StatsGranularity
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MissionAnalysisViewModelTest {

    private lateinit var viewModel: MissionAnalysisViewModel
    private val getMissionStatsUseCase: GetMissionStatsUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(emptyList())
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
    }

    // ============ Initial State tests ============

    @Test
    fun `initial state has loading true`() = runTest {
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(emptyList())
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        
        // Initial state before collect
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial state loads stats`() = runTest {
        val stats = listOf(
            MissionStatsEntry(label = "Mon", startDate = LocalDate.now(), completed = 3, missed = 1, inProgress = 2),
            MissionStatsEntry(label = "Tue", startDate = LocalDate.now().plusDays(1), completed = 5, missed = 0, inProgress = 1)
        )
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(stats)
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.stats.size)
        assertEquals("Mon", state.stats[0].label)
    }

    @Test
    fun `initial granularity is WEEK_OF_MONTH`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        assertEquals(StatsGranularity.WEEK_OF_MONTH, viewModel.uiState.value.granularity)
    }

    // ============ setGranularity tests ============

    @Test
    fun `setGranularity updates granularity to DAY_OF_WEEK`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        viewModel.setGranularity(StatsGranularity.DAY_OF_WEEK)
        advanceUntilIdle()
        
        assertEquals(StatsGranularity.DAY_OF_WEEK, viewModel.uiState.value.granularity)
    }

    @Test
    fun `setGranularity updates granularity to MONTH_OF_YEAR`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        viewModel.setGranularity(StatsGranularity.MONTH_OF_YEAR)
        advanceUntilIdle()
        
        assertEquals(StatsGranularity.MONTH_OF_YEAR, viewModel.uiState.value.granularity)
    }

    @Test
    fun `setGranularity triggers new stats fetch`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        clearMocks(getMissionStatsUseCase, answers = false)
        every { getMissionStatsUseCase.invoke(any(), StatsGranularity.DAY_OF_WEEK) } returns flowOf(emptyList())
        
        viewModel.setGranularity(StatsGranularity.DAY_OF_WEEK)
        advanceUntilIdle()
        
        verify { getMissionStatsUseCase.invoke(any(), StatsGranularity.DAY_OF_WEEK) }
    }

    // ============ prev navigation tests ============

    @Test
    fun `prev with DAY_OF_WEEK moves back one week`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.DAY_OF_WEEK)
        advanceUntilIdle()
        
        viewModel.prev()
        advanceUntilIdle()
        
        assertEquals(initialDate.minusWeeks(1), viewModel.uiState.value.referenceDate)
    }

    @Test
    fun `prev with WEEK_OF_MONTH moves back one month`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.WEEK_OF_MONTH)
        advanceUntilIdle()
        
        viewModel.prev()
        advanceUntilIdle()
        
        assertEquals(initialDate.minusMonths(1), viewModel.uiState.value.referenceDate)
    }

    @Test
    fun `prev with MONTH_OF_YEAR moves back one year`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.MONTH_OF_YEAR)
        advanceUntilIdle()
        
        viewModel.prev()
        advanceUntilIdle()
        
        assertEquals(initialDate.minusYears(1), viewModel.uiState.value.referenceDate)
    }

    // ============ next navigation tests ============

    @Test
    fun `next with DAY_OF_WEEK moves forward one week`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.DAY_OF_WEEK)
        advanceUntilIdle()
        
        viewModel.next()
        advanceUntilIdle()
        
        assertEquals(initialDate.plusWeeks(1), viewModel.uiState.value.referenceDate)
    }

    @Test
    fun `next with WEEK_OF_MONTH moves forward one month`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.WEEK_OF_MONTH)
        advanceUntilIdle()
        
        viewModel.next()
        advanceUntilIdle()
        
        assertEquals(initialDate.plusMonths(1), viewModel.uiState.value.referenceDate)
    }

    @Test
    fun `next with MONTH_OF_YEAR moves forward one year`() = runTest {
        createViewModel()
        advanceUntilIdle()
        
        val initialDate = viewModel.uiState.value.referenceDate
        
        viewModel.setGranularity(StatsGranularity.MONTH_OF_YEAR)
        advanceUntilIdle()
        
        viewModel.next()
        advanceUntilIdle()
        
        assertEquals(initialDate.plusYears(1), viewModel.uiState.value.referenceDate)
    }

    // ============ Total calculation tests ============

    @Test
    fun `totalCompleted returns sum of all completed counts`() = runTest {
        val stats = listOf(
            MissionStatsEntry(label = "Mon", startDate = LocalDate.now(), completed = 3, missed = 0, inProgress = 0),
            MissionStatsEntry(label = "Tue", startDate = LocalDate.now().plusDays(1), completed = 5, missed = 0, inProgress = 0),
            MissionStatsEntry(label = "Wed", startDate = LocalDate.now().plusDays(2), completed = 2, missed = 0, inProgress = 0)
        )
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(stats)
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        assertEquals(10, viewModel.totalCompleted())
    }

    @Test
    fun `totalMissed returns sum of all missed counts`() = runTest {
        val stats = listOf(
            MissionStatsEntry(label = "Mon", startDate = LocalDate.now(), completed = 0, missed = 2, inProgress = 0),
            MissionStatsEntry(label = "Tue", startDate = LocalDate.now().plusDays(1), completed = 0, missed = 3, inProgress = 0)
        )
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(stats)
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        assertEquals(5, viewModel.totalMissed())
    }

    @Test
    fun `totalInProgress returns sum of all in progress counts`() = runTest {
        val stats = listOf(
            MissionStatsEntry(label = "Mon", startDate = LocalDate.now(), completed = 0, missed = 0, inProgress = 4),
            MissionStatsEntry(label = "Tue", startDate = LocalDate.now().plusDays(1), completed = 0, missed = 0, inProgress = 6)
        )
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(stats)
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        assertEquals(10, viewModel.totalInProgress())
    }

    @Test
    fun `totalMissions returns sum of all counts`() = runTest {
        val stats = listOf(
            MissionStatsEntry(label = "Mon", startDate = LocalDate.now(), completed = 3, missed = 2, inProgress = 1),
            MissionStatsEntry(label = "Tue", startDate = LocalDate.now().plusDays(1), completed = 4, missed = 1, inProgress = 2)
        )
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(stats)
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        // Total = (3+4) + (2+1) + (1+2) = 7 + 3 + 3 = 13
        assertEquals(13, viewModel.totalMissions())
    }

    @Test
    fun `totals return 0 when no stats`() = runTest {
        every { getMissionStatsUseCase.invoke(any(), any()) } returns flowOf(emptyList())
        
        viewModel = MissionAnalysisViewModel(getMissionStatsUseCase)
        advanceUntilIdle()
        
        assertEquals(0, viewModel.totalCompleted())
        assertEquals(0, viewModel.totalMissed())
        assertEquals(0, viewModel.totalInProgress())
        assertEquals(0, viewModel.totalMissions())
    }
}
