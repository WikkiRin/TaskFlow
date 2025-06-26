package com.pet.taskflow.mapper

import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals

class BoardMapperTest {

    private val mapper = BoardMapper()

    @Test
    fun `toDto should map Board to BoardDto correctly`() {
        // GIVEN
        val owner = User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        val board = Board(id = 10L, title = "Project A", owner = owner)

        // WHEN
        val dto: BoardDto = mapper.toDto(board)

        // THEN
        assertEquals(board.id, dto.id)
        assertEquals(board.title, dto.title)
        assertEquals(board.owner.id, dto.ownerId)
        assertEquals(1L, dto.ownerId)
    }

    @Test
    fun `toEntity should map BoardRequest and User to Board correctly`() {
        // GIVEN
        val request = BoardRequest(title = "Project B")
        val owner = User(id = 2L, username = "user-2", email = "user-2@ex.com", password = "user-2-pass")

        // WHEN
        val entity: Board = mapper.toEntity(request, owner)

        // THEN
        assertEquals(request.title, entity.title)
        assertEquals(owner, entity.owner)
        // id = 0 для новой сущности
        assertEquals(0L, entity.id)
    }

    @Test
    fun `toEntity should handle empty title`() {
        // GIVEN
        val request = BoardRequest(title = "")
        val owner = User(id = 3L, username = "user-3", email = "user-3@ex.com", password = "user-3-pass")

        // WHEN
        val entity = mapper.toEntity(request, owner)

        // THEN
        assertEquals("", entity.title)
    }

}