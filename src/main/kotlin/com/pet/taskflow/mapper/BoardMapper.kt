package com.pet.taskflow.mapper

import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.User
import org.springframework.stereotype.Component

@Component
class BoardMapper {

    fun toDto(board: Board): BoardDto = BoardDto(
        id = board.id,
        title = board.title,
        ownerId = board.owner.id
    )

    fun toEntity(request: BoardRequest, owner: User): Board = Board(
        title = request.title,
        owner = owner
    )
}