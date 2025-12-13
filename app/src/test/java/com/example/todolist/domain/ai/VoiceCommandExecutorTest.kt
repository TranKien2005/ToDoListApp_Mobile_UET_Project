package com.example.todolist.domain.ai

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.domain.ai.models.CommandParams
import com.example.todolist.domain.ai.models.VoiceAction
import com.example.todolist.domain.ai.models.VoiceCommand
import com.example.todolist.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class VoiceCommandExecutorTest {

    private lateinit var executor: VoiceCommandExecutor
    private lateinit var taskUseCases: TaskUseCases
    private lateinit var missionUseCases: MissionUseCases

    // Mock individual use cases
    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val createTaskUseCase: CreateTaskUseCase = mockk()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val getTasksByDayUseCase: GetTasksByDayUseCase = mockk()
    private val getTasksByMonthUseCase: GetTasksByMonthUseCase = mockk()

    private val getMissionsUseCase: GetMissionsUseCase = mockk()
    private val createMissionUseCase: CreateMissionUseCase = mockk()
    private val updateMissionUseCase: UpdateMissionUseCase = mockk()
    private val deleteMissionUseCase: DeleteMissionUseCase = mockk()
    private val setMissionStatusUseCase: SetMissionStatusUseCase = mockk()
    private val getMissionsByDateUseCase: GetMissionsByDateUseCase = mockk()
    private val getMissionsByMonthUseCase: GetMissionsByMonthUseCase = mockk()
    private val getMissionStatsUseCase: GetMissionStatsUseCase = mockk()

    @Before
    fun setup() {
        taskUseCases = TaskUseCases(
            getTasks = getTasksUseCase,
            createTask = createTaskUseCase,
            updateTask = updateTaskUseCase,
            deleteTask = deleteTaskUseCase,
            getTasksByDay = getTasksByDayUseCase,
            getTasksByMonth = getTasksByMonthUseCase
        )

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

        executor = VoiceCommandExecutor(taskUseCases, missionUseCases)
    }

    // ============ CREATE_TASK tests ============

    @Test
    fun `execute CREATE_TASK creates task with provided params`() = runTest {
        coEvery { createTaskUseCase.invoke(any()) } just runs

        val command = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(
                title = "Team Meeting",
                description = "Weekly sync",
                date = "15/12/2024",
                time = "14:00",
                duration = 60
            ),
            responseText = "Đã tạo task Team Meeting"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertEquals("Đã tạo task Team Meeting", result.getOrNull())
        coVerify { createTaskUseCase.invoke(match { it.title == "Team Meeting" }) }
    }

    @Test
    fun `execute CREATE_TASK uses defaults for missing params`() = runTest {
        coEvery { createTaskUseCase.invoke(any()) } just runs

        val command = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = "Quick task"),
            responseText = "Task created"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { 
            createTaskUseCase.invoke(match { 
                it.title == "Quick task" && 
                it.durationMinutes == 60L &&
                it.repeatType == RepeatType.NONE
            }) 
        }
    }

    @Test
    fun `execute CREATE_TASK handles null title with default`() = runTest {
        coEvery { createTaskUseCase.invoke(any()) } just runs

        val command = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = null),
            responseText = "Task created"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { createTaskUseCase.invoke(match { it.title == "Untitled Task" }) }
    }

    // ============ CREATE_MISSION tests ============

    @Test
    fun `execute CREATE_MISSION creates mission with provided params`() = runTest {
        coEvery { createMissionUseCase.invoke(any()) } returns 1

        val command = VoiceCommand(
            action = VoiceAction.CREATE_MISSION,
            params = CommandParams(
                title = "Complete Project",
                description = "Finish all features",
                date = "25/12/2024",
                time = "23:59"
            ),
            responseText = "Đã tạo mission Complete Project"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertEquals("Đã tạo mission Complete Project", result.getOrNull())
        coVerify { createMissionUseCase.invoke(match { 
            it.title == "Complete Project" &&
            it.storedStatus == MissionStoredStatus.UNSPECIFIED
        }) }
    }

    @Test
    fun `execute CREATE_MISSION uses defaults for missing date and time`() = runTest {
        coEvery { createMissionUseCase.invoke(any()) } returns 1

        val command = VoiceCommand(
            action = VoiceAction.CREATE_MISSION,
            params = CommandParams(title = "Future mission"),
            responseText = "Mission created"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { createMissionUseCase.invoke(match { 
            it.title == "Future mission" 
        }) }
    }

    // ============ LIST_TASKS tests ============

    @Test
    fun `execute LIST_TASKS returns empty message when no tasks`() = runTest {
        every { getTasksUseCase.invoke() } returns flowOf(emptyList())

        val command = VoiceCommand(
            action = VoiceAction.LIST_TASKS,
            params = CommandParams(),
            responseText = "Listing tasks"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertEquals("Bạn chưa có task nào.", result.getOrNull())
    }

    @Test
    fun `execute LIST_TASKS returns formatted task list`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", startTime = LocalDateTime.of(2024, 12, 15, 10, 0)),
            Task(id = 2, title = "Task 2", startTime = LocalDateTime.of(2024, 12, 16, 11, 0))
        )
        every { getTasksUseCase.invoke() } returns flowOf(tasks)

        val command = VoiceCommand(
            action = VoiceAction.LIST_TASKS,
            params = CommandParams(),
            responseText = "Listing tasks"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("Bạn có 2 tasks"))
        assertTrue(result.getOrNull()!!.contains("Task 1"))
        assertTrue(result.getOrNull()!!.contains("Task 2"))
    }

    // ============ LIST_MISSIONS tests ============

    @Test
    fun `execute LIST_MISSIONS returns empty message when no missions`() = runTest {
        every { getMissionsUseCase.invoke() } returns flowOf(emptyList())

        val command = VoiceCommand(
            action = VoiceAction.LIST_MISSIONS,
            params = CommandParams(),
            responseText = "Listing missions"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertEquals("Bạn chưa có mission nào.", result.getOrNull())
    }

    @Test
    fun `execute LIST_MISSIONS returns formatted mission list`() = runTest {
        val missions = listOf(
            Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.of(2024, 12, 20, 23, 59)),
            Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.of(2024, 12, 25, 23, 59))
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)

        val command = VoiceCommand(
            action = VoiceAction.LIST_MISSIONS,
            params = CommandParams(),
            responseText = "Listing missions"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("Bạn có 2 missions"))
    }

    // ============ COMPLETE_TASK tests ============

    @Test
    fun `execute COMPLETE_TASK returns failure as tasks dont have completion state`() = runTest {
        val command = VoiceCommand(
            action = VoiceAction.COMPLETE_TASK,
            params = CommandParams(title = "Some task"),
            responseText = "Completing task"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Tasks không có completion state"))
    }

    // ============ COMPLETE_MISSION tests ============

    @Test
    fun `execute COMPLETE_MISSION completes existing mission`() = runTest {
        val missions = listOf(
            Mission(id = 42, title = "Target Mission", deadline = LocalDateTime.now().plusDays(1))
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)
        coEvery { setMissionStatusUseCase.invoke(42, MissionStoredStatus.COMPLETED) } just runs

        val command = VoiceCommand(
            action = VoiceAction.COMPLETE_MISSION,
            params = CommandParams(title = "Target Mission"),
            responseText = "Đã hoàn thành mission"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { setMissionStatusUseCase.invoke(42, MissionStoredStatus.COMPLETED) }
    }

    @Test
    fun `execute COMPLETE_MISSION fails if mission not found`() = runTest {
        every { getMissionsUseCase.invoke() } returns flowOf(emptyList())

        val command = VoiceCommand(
            action = VoiceAction.COMPLETE_MISSION,
            params = CommandParams(title = "Nonexistent"),
            responseText = "Completing mission"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Mission không tìm thấy"))
    }

    @Test
    fun `execute COMPLETE_MISSION fails if no title provided`() = runTest {
        // Mock is needed because completeMission calls getMissionsUseCase first
        every { getMissionsUseCase.invoke() } returns flowOf(emptyList())
        
        val command = VoiceCommand(
            action = VoiceAction.COMPLETE_MISSION,
            params = CommandParams(title = null),
            responseText = "Completing mission"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Title is required"))
    }

    // ============ DELETE_TASK tests ============

    @Test
    fun `execute DELETE_TASK deletes existing task`() = runTest {
        val tasks = listOf(
            Task(id = 10, title = "Task to delete", startTime = LocalDateTime.now())
        )
        every { getTasksUseCase.invoke() } returns flowOf(tasks)
        coEvery { deleteTaskUseCase.invoke(10) } just runs

        val command = VoiceCommand(
            action = VoiceAction.DELETE_TASK,
            params = CommandParams(title = "Task to delete"),
            responseText = "Đã xóa task"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { deleteTaskUseCase.invoke(10) }
    }

    @Test
    fun `execute DELETE_TASK finds task by partial match`() = runTest {
        val tasks = listOf(
            Task(id = 5, title = "Long task name with details", startTime = LocalDateTime.now())
        )
        every { getTasksUseCase.invoke() } returns flowOf(tasks)
        coEvery { deleteTaskUseCase.invoke(5) } just runs

        val command = VoiceCommand(
            action = VoiceAction.DELETE_TASK,
            params = CommandParams(title = "task name"),
            responseText = "Deleted"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { deleteTaskUseCase.invoke(5) }
    }

    @Test
    fun `execute DELETE_TASK fails if task not found`() = runTest {
        every { getTasksUseCase.invoke() } returns flowOf(emptyList())

        val command = VoiceCommand(
            action = VoiceAction.DELETE_TASK,
            params = CommandParams(title = "Nonexistent"),
            responseText = "Deleting task"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Task không tìm thấy"))
    }

    // ============ DELETE_MISSION tests ============

    @Test
    fun `execute DELETE_MISSION deletes existing mission`() = runTest {
        val missions = listOf(
            Mission(id = 20, title = "Mission to delete", deadline = LocalDateTime.now().plusDays(1))
        )
        every { getMissionsUseCase.invoke() } returns flowOf(missions)
        coEvery { deleteMissionUseCase.invoke(20) } just runs

        val command = VoiceCommand(
            action = VoiceAction.DELETE_MISSION,
            params = CommandParams(title = "Mission to delete"),
            responseText = "Đã xóa mission"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        coVerify { deleteMissionUseCase.invoke(20) }
    }

    @Test
    fun `execute DELETE_MISSION fails if mission not found`() = runTest {
        every { getMissionsUseCase.invoke() } returns flowOf(emptyList())

        val command = VoiceCommand(
            action = VoiceAction.DELETE_MISSION,
            params = CommandParams(title = "Nonexistent"),
            responseText = "Deleting mission"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Mission không tìm thấy"))
    }

    // ============ UNKNOWN action tests ============

    @Test
    fun `execute UNKNOWN returns responseText`() = runTest {
        val command = VoiceCommand(
            action = VoiceAction.UNKNOWN,
            params = CommandParams(),
            responseText = "Tôi không hiểu yêu cầu của bạn"
        )

        val result = executor.execute(command)

        assertTrue(result.isSuccess)
        assertEquals("Tôi không hiểu yêu cầu của bạn", result.getOrNull())
    }

    // ============ Error handling tests ============

    @Test
    fun `execute returns failure when use case throws exception`() = runTest {
        coEvery { createTaskUseCase.invoke(any()) } throws RuntimeException("Database error")

        val command = VoiceCommand(
            action = VoiceAction.CREATE_TASK,
            params = CommandParams(title = "Test"),
            responseText = "Creating task"
        )

        val result = executor.execute(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Database error"))
    }
}
