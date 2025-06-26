package com.pet.taskflow.mapper

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.Task
import com.pet.taskflow.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class TaskMapperTest {

    private val mapper = TaskMapper()

    @Test
    fun `toDto should map Task to TaskDto correctly`() {
        // GIVEN
        val owner = User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        val column =
            BoardColumn(id = 10L, name = "To Do", position = 0, board = Board(id = 1L, title = "Project T", owner = owner))
        val assignee = User(id = 2L, username = "user-2", email = "user-2@ex.com", password = "user-2-pass")
        val task = Task(
            id = 100L,
            title = "Fix bug",
            description = "Critical bug",
            position = 3,
            boardColumn = column,
            assignee = assignee
        )

        // WHEN
        val dto = mapper.toDto(task)

        // THEN
        assertEquals(task.id, dto.id)
        assertEquals(task.title, dto.title)
        assertEquals(task.description, dto.description)
        assertEquals(column.id, dto.columnId)
        assertEquals(assignee.id, dto.assigneeId)
        assertEquals(task.position, dto.position)
    }

    @Test
    fun `toDto should handle null assignee`() {
        // GIVEN
        val owner = User(id = 3L, username = "user-3", email = "user-3@ex.com", password = "user-3-pass")
        val column = BoardColumn(id = 10L, name = "To Do", position = 0, board = Board(id = 1L, title = "Project K", owner = owner))
        val task = Task(
            id = 101L,
            title = "Write docs",
            description = null,
            position = 1,
            boardColumn = column,
            assignee = null
        )

        // WHEN
        val dto = mapper.toDto(task)

        // THEN
        assertNull(dto.assigneeId)
        assertEquals(101L, dto.id)
    }

    @Test
    fun `toEntity should map CreateTaskRequest to Task correctly`() {
        // GIVEN
        val request = CreateTaskRequest(
            title = "New Task",
            description = "Some desc",
            columnId = 1L,
            assigneeId = 2L,
            position = null
        )
        val owner = User(id = 4L, username = "user-4", email = "user-4@ex.com", password = "user-4-pass")
        val column = BoardColumn(id = 1L, name = "Inbox", position = 0, board = Board(id = 9L, title = "Project B", owner = owner))
        val assignee = User(id = 2L, username = "user-5", email = "user-5@ex.com", password = "user-5-pass")
        val position = 5

        // WHEN
        val task = mapper.toEntity(request, column, assignee, position)

        // THEN
        assertEquals(request.title, task.title)
        assertEquals(request.description, task.description)
        assertEquals(column, task.boardColumn)
        assertEquals(assignee, task.assignee)
        assertEquals(position, task.position)
    }

    @Test
    fun `updateEntity should override fields in Task`() {
        // GIVEN
        val owner = User(id = 5L, username = "user-5", email = "user-5@ex.com", password = "user-5-pass")
        val existing = Task(
            id = 200L,
            title = "Old Title",
            description = "Old Desc",
            position = 0,
            boardColumn = BoardColumn(id = 1, name = "", position = 0, board = Board(id = 1, title = "Project D", owner = owner)),
            assignee = null
        )
        val request = UpdateTaskRequest(
            title = "New Title",
            description = "Updated Desc",
            assigneeId = 6L,
            position = 2
        )
        val assignee = User(id = 6L, username = "user-6", email = "user-6@ex.com", password = "ser-6-pass")

        // WHEN
        val updated = mapper.updateEntity(existing, request, assignee)

        // THEN
        assertEquals("New Title", updated.title)
        assertEquals("Updated Desc", updated.description)
        assertEquals(2, updated.position)
        assertEquals(assignee, updated.assignee)
    }

    @Test
    fun `updateEntity should preserve position if null in request`() {
        // GIVEN
        val owner = User(id = 7L, username = "user-7", email = "user-7@ex.com", password = "user-7-pass")
        val existing = Task(
            id = 300L,
            title = "Test",
            description = null,
            position = 7,
            boardColumn = BoardColumn(id = 1L, name = "", position = 0, board = Board(id = 1L, title = "Project S", owner = owner)),
            assignee = null
        )
        val request = UpdateTaskRequest(
            title = "Updated",
            description = "Changed",
            assigneeId = null,
            position = null
        )

        // WHEN
        val updated = mapper.updateEntity(existing, request, assignee = null)

        // THEN
        assertEquals(7, updated.position)
        assertEquals("Updated", updated.title)
        assertEquals("Changed", updated.description)
        assertNull(updated.assignee)
    }

}