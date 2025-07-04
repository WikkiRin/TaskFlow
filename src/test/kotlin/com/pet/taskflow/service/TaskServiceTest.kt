package com.pet.taskflow.service

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.TaskDto
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.Task
import com.pet.taskflow.entity.User
import com.pet.taskflow.mapper.TaskMapper
import com.pet.taskflow.repository.TaskRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.*
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskServiceTest {

    @MockK
    lateinit var taskRepository: TaskRepository

    @MockK
    lateinit var taskMapper: TaskMapper

    @MockK
    lateinit var boardColumnService: BoardColumnService

    @MockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var testObj: TaskService

    @BeforeAll
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @BeforeEach
    fun setup() {
        clearMocks(taskRepository, taskMapper, boardColumnService, userService)
    }

    @Test
    fun `createTask should use provided position if not null`() {
        // GIVEN
        val request = CreateTaskRequest(
            title = "Task-1",
            description = "Description-1",
            columnId = 1L,
            assigneeId = 2L,
            position = 3
        )
        val column = BoardColumn(id = 1L, name = "To do", board = mockk(), position = 0)
        val assignee = User(id = 2L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        val task = Task(
            id = 100L,
            title = "Task-1",
            description = "Description-1",
            boardColumn = column,
            assignee = assignee,
            position = 3
        )
        val dto = TaskDto(
            id = 100L,
            title = "Task-1",
            description = "Description-1",
            columnId = 1L,
            assigneeId = 2L,
            position = 3
        )

        every { boardColumnService.findById(1L) } returns column
        every { userService.findById(2L) } returns assignee
        every { taskMapper.toEntity(request, column, assignee, 3) } returns task
        every { taskRepository.save(task) } returns task
        every { taskMapper.toDto(task) } returns dto

        // WHEN
        val result = testObj.createTask(request)

        // THEN
        assert(result == dto)
        verifySequence {
            boardColumnService.findById(1L)
            userService.findById(2L)
            taskMapper.toEntity(request, column, assignee, 3)
            taskRepository.save(task)
            taskMapper.toDto(task)
        }
        verify(exactly = 0) { taskRepository.countByBoardColumnId(any()) }
    }

    @Test
    fun `createTask should use fallback position if position is null`() {
        // GIVEN
        val request = CreateTaskRequest(
            title = "Task-1",
            description = "Description-1",
            columnId = 2L,
            assigneeId = null,
            position = null
        )
        val column = BoardColumn(id = 2L, name = "To do", board = mockk(), position = 0)
        val task = Task(
            id = 101L,
            title = "Task-2",
            description = "Description-2",
            boardColumn = column,
            assignee = null,
            position = 2
        )
        val dto = TaskDto(
            id = 101L,
            title = "Task-2",
            description = "Description-2",
            columnId = 1L,
            assigneeId = null,
            position = 2
        )

        every { boardColumnService.findById(2L) } returns column
        every { taskRepository.countByBoardColumnId(2L) } returns 2
        every { taskMapper.toEntity(request, column, null, 2) } returns task
        every { taskRepository.save(task) } returns task
        every { taskMapper.toDto(task) } returns dto

        // WHEN
        val result = testObj.createTask(request)

        // THEN
        assert(result == dto)

        verifySequence {
            boardColumnService.findById(2L)
            taskRepository.countByBoardColumnId(2L)
            taskMapper.toEntity(request, column, null, 2)
            taskRepository.save(task)
            taskMapper.toDto(task)
        }
    }

    @Test
    fun `getTasksByColumn should return list of TaskDto`() {
        // GIVEN
        val column = BoardColumn(id = 3L, name = "To do", board = mockk(), position = 0)
        val tasks = listOf(
            Task(
                id = 103L,
                title = "Task-3",
                description = "Description-3",
                boardColumn = column,
                assignee = null,
                position = 0
            ),
            Task(
                id = 104L,
                title = "Task-4",
                description = "Description-4",
                boardColumn = column,
                assignee = null,
                position = 1
            )
        )
        val dtos = listOf(
            TaskDto(
                id = 103L,
                title = "Task-3",
                description = "Description-3",
                columnId = column.id,
                assigneeId = null,
                position = 0
            ),
            TaskDto(
                id = 104L,
                title = "Task-4",
                description = "Description-4",
                columnId = column.id,
                assigneeId = null,
                position = 1
            )
        )
        every { taskRepository.findAllByBoardColumnIdOrderByPosition(3L) } returns tasks
        every { taskMapper.toDto(tasks[0]) } returns dtos[0]
        every { taskMapper.toDto(tasks[1]) } returns dtos[1]

        // WHEN
        val result = testObj.getTasksByColumn(3L)

        // THEN
        assert(result == dtos)

        verify { taskRepository.findAllByBoardColumnIdOrderByPosition(3L) }
        verify(exactly = 2) { taskMapper.toDto(any()) }

    }

    @Test
    fun `findById should return task if exists`() {
        // GIVEN
        val task = mockk<Task>()
        every { taskRepository.findById(10L) } returns Optional.of(task)

        // WHEN
        val result = testObj.findById(10L)

        // THEN
        assert(result == task)
        verify { taskRepository.findById(10L) }
    }

    @Test
    fun `findById should throw if not found`() {
        // GIVEN
        val id = 10L
        every { taskRepository.findById(id) } returns Optional.empty()

        // WHEN
        val ex = assertThrows<EntityNotFoundException> {
            testObj.findById(id)
        }

        // THEN
        assert(ex.message == "Задача с id=$id не найдена")
    }

    @Test
    fun `updateTask should update and return TaskDto`() {
        // GIVEN
        val request = UpdateTaskRequest(
            title = "Updated",
            description = "Description-Updated",
            assigneeId = 3L,
            position = 1
        )
        val assignee = User(id = 3L, username = "user-3", email = "user-3@ex.com", password = "user-3-pass")
        val column = BoardColumn(id = 1L, name = "To do", board = mockk(), position = 0)
        val oldTask = Task(
            id = 103L,
            title = "Task-Old",
            description = "Description-Old",
            boardColumn = column,
            assignee = null,
            position = 0
        )
        val updatedTask = oldTask.copy(title = "Updated", assignee = assignee)
        val dto =
            TaskDto(
                id = 103L,
                title = "Updated",
                description = "Description-Old",
                columnId = column.id,
                assigneeId = assignee.id,
                position = 0
            )
        val service = spyk(testObj)

        every { service.findById(103L) } returns oldTask
        every { userService.findById(3L) } returns assignee
        every { taskMapper.updateEntity(oldTask, request, assignee) } returns updatedTask
        every { taskRepository.save(updatedTask) } returns updatedTask
        every { taskMapper.toDto(updatedTask) } returns dto

        // WHEN
        val result = service.updateTask(103L, request)

        // THEN
        assert(result == dto)
        verify { service.findById(103L) }
        verify { userService.findById(3L) }
        verify { taskMapper.updateEntity(oldTask, request, assignee) }
        verify { taskRepository.save(updatedTask) }
        verify { taskMapper.toDto(updatedTask) }
    }

    @Test
    fun `updateTask should update task with null assignee`() {
        // GIVEN
        val request = UpdateTaskRequest(
            title = "Updated",
            description = "Description-Updated",
            assigneeId = null,
            position = 2
        )

        val column = BoardColumn(id = 2L, name = "To do", board = mockk(), position = 0)
        val oldTask = Task(
            id = 104L,
            title = "Task-Old",
            description = "Description-Old",
            boardColumn = column,
            assignee = null,
            position = 0
        )
        val updatedTask = oldTask.copy(title = "Updated", description = "Updated-description", assignee = null)
        val dto =
            TaskDto(
                id = 104L,
                title = "Updated",
                description = "Description-description",
                columnId = column.id,
                assigneeId = null,
                position = 0
            )
        val service = spyk(testObj)

        every { service.findById(104L) } returns oldTask
        every { taskMapper.updateEntity(oldTask, request, null) } returns updatedTask
        every { taskRepository.save(updatedTask) } returns updatedTask
        every { taskMapper.toDto(updatedTask) } returns dto

        // WHEN
        val result = service.updateTask(104L, request)

        // THEN
        assert(result == dto)

        verify { service.findById(104L) }
        verify { taskMapper.updateEntity(oldTask, request, null) }
        verify { taskRepository.save(updatedTask) }
        verify { taskMapper.toDto(updatedTask) }

        verify(exactly = 0) { userService.findById(any()) }
    }

    @Test
    fun `deleteTask should remove task if exists`() {
        // GIVEN
        every { taskRepository.existsById(1L) } returns true
        every { taskRepository.deleteById(1L) } just Runs

        // WHEN
        testObj.deleteTask(1L)

        // THEN
        verifySequence {
            taskRepository.existsById(1L)
            taskRepository.deleteById(1L)
        }
    }

    @Test
    fun `deleteTask should throw if task not exists`() {
        // GIVEN
        val id = 10L
        every { taskRepository.existsById(id) } returns false

        // WHEN
        val ex = assertThrows<EntityNotFoundException> {
            testObj.deleteTask(id)
        }

        // THEN
        assert(ex.message == "Задача с id=$id не найдена")
    }
}