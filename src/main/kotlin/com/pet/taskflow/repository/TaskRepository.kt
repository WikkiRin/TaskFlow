package com.pet.taskflow.repository

import com.pet.taskflow.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    /**
     * Возвращает все задачи, принадлежащие колонке, отсортированные по позиции.
     *
     * @param boardColumn ID колонки
     * @return список задач
     */
    fun findAllByBoardColumnIdOrderByPosition(boardColumn: Long): List<Task>

    /**
     * Возвращает количество задач в колонке.
     *
     * @param boardColumn ID колонки
     * @return количество задач
     */
    fun countByBoardColumnId(boardColumn: Long): Long
}