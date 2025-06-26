package com.pet.taskflow.mapper

import com.pet.taskflow.dto.BoardColumnDto
import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.BoardColumn
import org.springframework.stereotype.Component

@Component
class BoardColumnMapper {

    fun toDto(column: BoardColumn): BoardColumnDto = BoardColumnDto(
        id = column.id,
        name = column.name,
        position = column.position,
        boardId = column.board.id
    )

    fun toEntity(request: CreateColumnRequest, board: Board, position: Int): BoardColumn = BoardColumn(
        name = request.name,
        position = position,
        board = board
    )

    fun updateEntity(existing: BoardColumn, request: UpdateColumnRequest): BoardColumn = existing.copy(
        name = request.name,
        position = request.position ?: existing.position
    )
}