package com.pet.taskflow.repository

import com.pet.taskflow.entity.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardRepository : JpaRepository<Board, Long> {
    fun findAllByOwnerId(ownerId: Long): List<Board>
}