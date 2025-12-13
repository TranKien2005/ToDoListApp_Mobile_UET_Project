package com.example.todolist.feature.mission

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MissionViewModelTest {

    private lateinit var viewModel: MissionViewModel
    private lateinit var missionUseCases: MissionUseCases
    private lateinit var notificationUseCases: NotificationUseCases

    // Mock individual use cases
    private val getMissionsUseCase: GetMissionsUseCase = mockk()
    private val createMissionUseCase: CreateMissionUseCase = mockk()
    private val updateMissionUseCase: UpdateMissionUseCase = mockk()
    private val deleteMissionUseCase: DeleteMissionUseCase = mockk()
    private val setMissionStatusUseCase: SetMissionStatusUseCase = mockk()
    private val getMissionsByDateUseCase: GetMissionsByDateUseCase = mockk()
    private val getMissionsByMonthUseCase: GetMissionsByMonthUseCase = mockk()
    private val getMissionStatsUseCase: GetMissionStatsUseCase = mockk()

    // Notification use cases
    private val getNotificationsUseCase: GetNotificationsUseCase = mockk()
    private val scheduleTaskNotificationUseCase: ScheduleTaskNotificationUseCase = mockk()
    private val scheduleMissionNotificationUseCase: ScheduleMissionNotificationUseCase = mockk()
    private val cancelTaskNotificationsUseCase: CancelTaskNotificationsUseCase = mockk()
    private val cancelMissionNotificationsUseCase: CancelMissionNotificationsUseCase = mockk()
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase = mockk()
    private val deleteReadNotificationsUseCase: DeleteReadNotificationsUseCase = mockk()
    private val createNotificationUseCase: CreateNotificationUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        missionUseCases = MissionUseCases(
            getMissions = getMissionsUseCase,
            createMission = createMissionUseCase,
            updateMission = updateMissionUseCase,
            deleteMission = deleteMissionUseCase,
            setMissionStatus = setMissionStatusUseCase,
            getMissionsByDate = getMissionsByDateUseCase,
            getMissionsByMonth = getMissionsByMonthUseCase,
            getMissionStats = getMissionStatsUseCase
        )

        notificationUseCases = NotificationUseCases(
            getNotifications = getNotificationsUseCase,
            scheduleTaskNotification = scheduleTaskNotificationUseCase,
            scheduleMissionNotification = scheduleMissionNotificationUseCase,
            cancelTaskNotifications = cancelTaskNotificationsUseCase,
            cancelMissionNotifications = cancelMissionNotificationsUseCase,
            markNotificationAsRead = markNotificationAsReadUseCase,
            deleteReadNotifications = deleteReadNotificationsUseCase,
            createNotification = createNotificationUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        every { getMissionsUseCase.invoke() } returns flowOf(emptyList())
        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
    }

    // ============ Initial State tests ============

    @Test
    fun `initial state has correct defaults`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(MissionTag.ALL, state.selectedTag)
        assertEquals(MissionStatusFilter.ALL, state.statusFilter)
        assertEquals(StatsGranularity.WEEK_OF_MONTH, state.granularity)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initial state loads missions`() = runTest {
        val missions = listOf(
            Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.now().plusDays(1)),
            Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.now().plusDays(2))
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.missions.isNotEmpty())
    }

    // ============ selectTag tests ============

    @Test
    fun `selectTag updates selected tag`() = runTest {
        createViewModel()
        advanceUntilIdle()

        viewModel.selectTag(MissionTag.TODAY)
        advanceUntilIdle()

        assertEquals(MissionTag.TODAY, viewModel.uiState.value.selectedTag)
    }

    @Test
    fun `selectTag filters missions by tag`() = runTest {
        val today = LocalDate.now()
        val missions = listOf(
            Mission(id = 1, title = "Today Mission", deadline = today.atTime(18, 0)),
            Mission(id = 2, title = "Tomorrow Mission", deadline = today.plusDays(1).atTime(18, 0))
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.selectTag(MissionTag.TODAY)
        advanceUntilIdle()

        val filteredMissions = viewModel.uiState.value.missions
        assertEquals(1, filteredMissions.size)
        assertEquals("Today Mission", filteredMissions[0].title)
    }

    // ============ setStatusFilter tests ============

    @Test
    fun `setStatusFilter updates status filter`() = runTest {
        createViewModel()
        advanceUntilIdle()

        viewModel.setStatusFilter(MissionStatusFilter.COMPLETED)
        advanceUntilIdle()

        assertEquals(MissionStatusFilter.COMPLETED, viewModel.uiState.value.statusFilter)
    }

    @Test
    fun `setStatusFilter COMPLETED shows only completed missions`() = runTest {
        val missions = listOf(
            Mission(id = 1, title = "Active", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED),
            Mission(id = 2, title = "Done", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.COMPLETED)
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.setStatusFilter(MissionStatusFilter.COMPLETED)
        advanceUntilIdle()

        val filteredMissions = viewModel.uiState.value.missions
        assertEquals(1, filteredMissions.size)
        assertEquals("Done", filteredMissions[0].title)
    }

    @Test
    fun `setStatusFilter IN_PROGRESS shows only active missions`() = runTest {
        val missions = listOf(
            Mission(id = 1, title = "Active", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED),
            Mission(id = 2, title = "Done", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.COMPLETED)
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.setStatusFilter(MissionStatusFilter.IN_PROGRESS)
        advanceUntilIdle()

        val filteredMissions = viewModel.uiState.value.missions
        assertEquals(1, filteredMissions.size)
        assertEquals("Active", filteredMissions[0].title)
    }

    // ============ setGranularity tests ============

    @Test
    fun `setGranularity updates granularity`() = runTest {
        createViewModel()
        advanceUntilIdle()

        viewModel.setGranularity(StatsGranularity.DAY_OF_WEEK)
        advanceUntilIdle()

        assertEquals(StatsGranularity.DAY_OF_WEEK, viewModel.uiState.value.granularity)
    }

    // ============ prev/next navigation tests ============

    @Test
    fun `prev moves reference date back by week for DAY_OF_WEEK granularity`() = runTest {
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
    fun `next moves reference date forward by week for DAY_OF_WEEK granularity`() = runTest {
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
    fun `prev moves reference date back by month for WEEK_OF_MONTH granularity`() = runTest {
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
    fun `next moves reference date forward by month for WEEK_OF_MONTH granularity`() = runTest {
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
    fun `prev moves reference date back by year for MONTH_OF_YEAR granularity`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val initialDate = viewModel.uiState.value.referenceDate

        viewModel.setGranularity(StatsGranularity.MONTH_OF_YEAR)
        advanceUntilIdle()

        viewModel.prev()
        advanceUntilIdle()

        assertEquals(initialDate.minusYears(1), viewModel.uiState.value.referenceDate)
    }

    // ============ deleteMission tests ============

    @Test
    fun `deleteMission calls deleteMission use case`() = runTest {
        createViewModel()
        advanceUntilIdle()

        coEvery { deleteMissionUseCase.invoke(42) } just runs

        viewModel.deleteMission(42)
        advanceUntilIdle()

        coVerify { deleteMissionUseCase.invoke(42) }
    }

    // ============ toggleMissionCompleted tests ============

    @Test
    fun `toggleMissionCompleted sets UNSPECIFIED mission to COMPLETED`() = runTest {
        val missions = listOf(
            Mission(id = 10, title = "Active Mission", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED)
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)
        coEvery { setMissionStatusUseCase.invoke(10, MissionStoredStatus.COMPLETED) } just runs
        coEvery { cancelMissionNotificationsUseCase.invoke(10) } just runs

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.toggleMissionCompleted(10)
        advanceUntilIdle()

        coVerify { setMissionStatusUseCase.invoke(10, MissionStoredStatus.COMPLETED) }
        coVerify { cancelMissionNotificationsUseCase.invoke(10) }
    }

    @Test
    fun `toggleMissionCompleted sets COMPLETED mission to UNSPECIFIED`() = runTest {
        val missions = listOf(
            Mission(id = 20, title = "Completed Mission", deadline = LocalDateTime.now().plusDays(1), storedStatus = MissionStoredStatus.COMPLETED)
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)
        coEvery { setMissionStatusUseCase.invoke(20, MissionStoredStatus.UNSPECIFIED) } just runs

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.toggleMissionCompleted(20)
        advanceUntilIdle()

        coVerify { setMissionStatusUseCase.invoke(20, MissionStoredStatus.UNSPECIFIED) }
        // Should NOT cancel notifications when un-completing
        coVerify(exactly = 0) { cancelMissionNotificationsUseCase.invoke(any()) }
    }

    @Test
    fun `toggleMissionCompleted does nothing for MISSED mission`() = runTest {
        val missions = listOf(
            Mission(id = 30, title = "Missed Mission", deadline = LocalDateTime.now().minusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED)
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        viewModel = MissionViewModel(missionUseCases, notificationUseCases)
        advanceUntilIdle()

        viewModel.toggleMissionCompleted(30)
        advanceUntilIdle()

        // Should NOT call setMissionStatus for MISSED missions
        coVerify(exactly = 0) { setMissionStatusUseCase.invoke(any(), any()) }
    }

    // ============ saveMission tests ============

    @Test
    fun `saveMission creates new mission when id is 0`() = runTest {
        createViewModel()
        advanceUntilIdle()

        coEvery { createMissionUseCase.invoke(any()) } returns 1

        val newMission = Mission(
            id = 0,
            title = "New Mission",
            deadline = LocalDateTime.now().plusDays(7)
        )

        viewModel.saveMission(newMission)
        advanceUntilIdle()

        coVerify { createMissionUseCase.invoke(newMission) }
        coVerify(exactly = 0) { updateMissionUseCase.invoke(any()) }
    }

    @Test
    fun `saveMission updates existing mission when id is not 0`() = runTest {
        createViewModel()
        advanceUntilIdle()

        coEvery { updateMissionUseCase.invoke(any()) } returns 50

        val existingMission = Mission(
            id = 50,
            title = "Updated Mission",
            deadline = LocalDateTime.now().plusDays(7)
        )

        viewModel.saveMission(existingMission)
        advanceUntilIdle()

        coVerify { updateMissionUseCase.invoke(existingMission) }
        coVerify(exactly = 0) { createMissionUseCase.invoke(any()) }
    }
}
