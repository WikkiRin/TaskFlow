package com.pet.taskflow.mapper

import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals


class BoardColumnMapperTest {

    private val mapper = BoardColumnMapper()

    @Test
    fun `toDto should map BoardColumn to BoardColumnDto correctly`() {
        // GIVEN
        val board = Board(
            id = 1L,
            title = "Project X",
            owner = User(id = 100L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        )
        val column = BoardColumn(id = 10L, name = "To Do", position = 1, board = board)

        // WHEN
        val dto = mapper.toDto(column)

        // THEN
        assertEquals(column.id, dto.id)
        assertEquals(column.name, dto.name)
        assertEquals(column.position, dto.position)
        assertEquals(board.id, dto.boardId)
    }

    @Test
    fun `toEntity should map CreateColumnRequest and Board to BoardColumn correctly`() {
        // GIVEN
        val board = Board(
            id = 2L,
            title = "Project C",
            owner = User(id = 2L, username = "user-2", email = "user-2@ex.com", password = "user-2-pass")
        )
        val request = CreateColumnRequest(name = "In Progress", boardId = board.id, position = null)
        val position = 5

        // WHEN
        val entity = mapper.toEntity(request, board, position)

        // THEN
        assertEquals(request.name, entity.name)
        assertEquals(position, entity.position)
        assertEquals(board, entity.board)
        // id = 0 для новой сущности
        assertEquals(0, entity.id)
    }

    @Test
    fun `updateEntity should override only name and position`() {
        // GIVEN
        val board = Board(id = 3L, title = "Project S", owner = User(3L, "user-3", "user-3@ex.com", "user-3-pass"))
        val original = BoardColumn(id = 33L, name = "Old", position = 0, board = board)
        val request = UpdateColumnRequest(name = "Updated", position = 2)

        // WHEN
        val updated = mapper.updateEntity(original, request)

        // THEN
        assertEquals(original.id, updated.id) // id не изменился
        assertEquals("Updated", updated.name)
        assertEquals(2, updated.position)
        assertEquals(board, updated.board)
    }

    @Test
    fun `updateEntity should keep existing position if new is null`() {
        // GIVEN
        val board = Board(id = 4L, title = "Board", owner = User(4L, "user-4", "user-4@ex.com", "user-4-pass"))
        val original = BoardColumn(id = 44L, name = "Old", position = 3, board = board)
        val request = UpdateColumnRequest(name = "Renamed", position = null)

        // WHEN
        val updated = mapper.updateEntity(original, request)

        // THEN
        assertEquals(3, updated.position) // позиция осталась прежней
        assertEquals("Renamed", updated.name)
    }
}