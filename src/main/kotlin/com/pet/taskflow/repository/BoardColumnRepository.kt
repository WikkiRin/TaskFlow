package com.pet.taskflow.repository

import com.pet.taskflow.entity.BoardColumn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardColumnRepository : JpaRepository<BoardColumn, Long> {
    fun findAllByBoardIdOrderByPosition(boardId: Long): List<BoardColumn>
}