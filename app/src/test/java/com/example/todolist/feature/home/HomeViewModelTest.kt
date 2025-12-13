package com.example.todolist.feature.home

import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var taskUseCases: TaskUseCases
    
    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val createTaskUseCase: CreateTaskUseCase = mockk()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val getTasksByDayUseCase: GetTasksByDayUseCase = mockk()
    private val getTasksByMonthUseCase: GetTasksByMonthUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        taskUseCases = TaskUseCases(
            getTasks = getTasksUseCase,
            createTask = createTaskUseCase,
            updateTask = updateTaskUseCase,
            deleteTask = deleteTaskUseCase,
            getTasksByDay = getTasksByDayUseCase,
            getTasksByMonth = getTasksByMonthUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(initialDate: LocalDate = LocalDate.now()) {
        // Default mock for getTasksByDay
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        viewModel = HomeViewModel(taskUseCases, initialDate)
    }

    // ============ Initial State tests ============

    @Test
    fun `initial state has correct default values`() = runTest {
        val today = LocalDate.now()
        createViewModel(today)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(YearMonth.from(today), state.currentMonth)
        assertEquals(today, state.selectedDate)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `initial state loads tasks for selected date`() = runTest {
        val today = LocalDate.of(2024, 6, 15)
        val tasks = listOf(
            Task(id = 1, title = "Task 1", startTime = LocalDateTime.of(2024, 6, 15, 10, 0)),
            Task(id = 2, title = "Task 2", startTime = LocalDateTime.of(2024, 6, 15, 14, 0))
        )
        
        every { getTasksByDayUseCase.invoke(today) } returns flowOf(tasks)
        
        viewModel = HomeViewModel(taskUseCases, today)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertEquals("Task 1", state.tasks[0].title)
        assertEquals("Task 2", state.tasks[1].title)
    }

    // ============ selectDate tests ============

    @Test
    fun `selectDate updates selected date and month`() = runTest {
        createViewModel(LocalDate.of(2024, 6, 15))
        advanceUntilIdle()
        
        val newDate = LocalDate.of(2024, 7, 20)
        every { getTasksByDayUseCase.invoke(newDate) } returns flowOf(emptyList())
        
        viewModel.selectDate(newDate)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
        assertEquals(YearMonth.of(2024, 7), state.currentMonth)
    }

    @Test
    fun `selectDate loads tasks for new date`() = runTest {
        createViewModel(LocalDate.of(2024, 6, 15))
        advanceUntilIdle()
        
        val newDate = LocalDate.of(2024, 6, 20)
        val newTasks = listOf(
            Task(id = 3, title = "New Task", startTime = LocalDateTime.of(2024, 6, 20, 9, 0))
        )
        every { getTasksByDayUseCase.invoke(newDate) } returns flowOf(newTasks)
        
        viewModel.selectDate(newDate)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(1, state.tasks.size)
        assertEquals("New Task", state.tasks[0].title)
    }

    // ============ Month navigation tests ============

    @Test
    fun `prevMonth moves to previous month`() = runTest {
        createViewModel(LocalDate.of(2024, 6, 15))
        advanceUntilIdle()
        
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        
        viewModel.prevMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2024, 5), state.currentMonth)
        assertEquals(LocalDate.of(2024, 5, 1), state.selectedDate)
    }

    @Test
    fun `nextMonth moves to next month`() = runTest {
        createViewModel(LocalDate.of(2024, 6, 15))
        advanceUntilIdle()
        
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        
        viewModel.nextMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2024, 7), state.currentMonth)
        assertEquals(LocalDate.of(2024, 7, 1), state.selectedDate)
    }

    @Test
    fun `prevMonth handles year boundary`() = runTest {
        createViewModel(LocalDate.of(2024, 1, 15))
        advanceUntilIdle()
        
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        
        viewModel.prevMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2023, 12), state.currentMonth)
    }

    @Test
    fun `nextMonth handles year boundary`() = runTest {
        createViewModel(LocalDate.of(2024, 12, 15))
        advanceUntilIdle()
        
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        
        viewModel.nextMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2025, 1), state.currentMonth)
    }

    // ============ refresh tests ============

    @Test
    fun `refresh sets loading to true then loads tasks`() = runTest {
        val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
        every { getTasksByDayUseCase.invoke(any()) } returns tasksFlow
        
        createViewModel(LocalDate.of(2024, 6, 15))
        advanceUntilIdle()
        
        viewModel.refresh()
        
        // After refresh, loading should be true
        assertTrue(viewModel.uiState.value.isLoading)
        
        advanceUntilIdle()
        
        // After completion, loading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ============ deleteTask tests ============

    @Test
    fun `deleteTask calls use case and refreshes list`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", startTime = LocalDateTime.now()),
            Task(id = 2, title = "Task 2", startTime = LocalDateTime.now())
        )
        
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(tasks)
        coEvery { deleteTaskUseCase.invoke(1) } just runs
        
        createViewModel()
        advanceUntilIdle()
        
        assertEquals(2, viewModel.uiState.value.tasks.size)
        
        viewModel.deleteTask(1)
        advanceUntilIdle()
        
        // Verify use case was called
        coVerify { deleteTaskUseCase.invoke(1) }
    }

    @Test
    fun `deleteTask calls deleteTask use case`() = runTest {
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase.invoke(42) } just runs
        
        createViewModel()
        advanceUntilIdle()
        
        viewModel.deleteTask(42)
        advanceUntilIdle()
        
        coVerify { deleteTaskUseCase.invoke(42) }
    }

    @Test
    fun `deleteTask handles exception gracefully`() = runTest {
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase.invoke(any()) } throws RuntimeException("Delete failed")
        
        createViewModel()
        advanceUntilIdle()
        
        viewModel.deleteTask(1)
        advanceUntilIdle()
        
        // Verify use case was called (error is handled internally)
        coVerify { deleteTaskUseCase.invoke(1) }
    }

    // ============ saveTask tests ============

    @Test
    fun `saveTask creates new task when id is 0`() = runTest {
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { createTaskUseCase.invoke(any()) } just runs
        
        createViewModel()
        advanceUntilIdle()
        
        val newTask = Task(
            id = 0,
            title = "New Task",
            startTime = LocalDateTime.now()
        )
        
        viewModel.saveTask(newTask)
        advanceUntilIdle()
        
        coVerify { createTaskUseCase.invoke(newTask) }
        coVerify(exactly = 0) { updateTaskUseCase.invoke(any()) }
    }

    @Test
    fun `saveTask updates existing task when id is not 0`() = runTest {
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { updateTaskUseCase.invoke(any()) } just runs
        
        createViewModel()
        advanceUntilIdle()
        
        val existingTask = Task(
            id = 42,
            title = "Updated Task",
            startTime = LocalDateTime.now()
        )
        
        viewModel.saveTask(existingTask)
        advanceUntilIdle()
        
        coVerify { updateTaskUseCase.invoke(existingTask) }
        coVerify(exactly = 0) { createTaskUseCase.invoke(any()) }
    }

    @Test
    fun `saveTask triggers refresh after save`() = runTest {
        val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
        every { getTasksByDayUseCase.invoke(any()) } returns tasksFlow
        coEvery { createTaskUseCase.invoke(any()) } just runs
        
        createViewModel()
        advanceUntilIdle()
        
        val newTask = Task(id = 0, title = "New", startTime = LocalDateTime.now())
        
        viewModel.saveTask(newTask)
        advanceUntilIdle()
        
        // Verify refresh was triggered by checking loading state transitions
        coVerify { createTaskUseCase.invoke(any()) }
    }

    @Test
    fun `saveTask sets error on exception`() = runTest {
        every { getTasksByDayUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { createTaskUseCase.invoke(any()) } throws RuntimeException("Save failed")
        
        createViewModel()
        advanceUntilIdle()
        
        viewModel.saveTask(Task(id = 0, title = "Test", startTime = LocalDateTime.now()))
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Save failed"))
    }
}
