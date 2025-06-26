package com.pet.taskflow.mapper

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.TaskDto
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.Task
import com.pet.taskflow.entity.User
import org.springframework.stereotype.Component

@Component
class TaskMapper {

    fun toDto(task: Task): TaskDto = TaskDto(
        id = task.id,
        title = task.title,
        description = task.description,
        columnId = task.boardColumn.id,
        assigneeId = task.assignee?.id,
        position = task.position
    )

    fun toEntity(
        request: CreateTaskRequest,
        column: BoardColumn,
        assignee: User?,
        position: Int
    ): Task = Task(
        title = request.title,
        description = request.description,
        position = position,
        assignee = assignee,
        boardColumn = column
    )

    fun updateEntity(
        existing: Task,
        request: UpdateTaskRequest,
        assignee: User?
    ): Task = existing.copy(
        title = request.title,
        description = request.description,
        position = request.position ?: existing.position,
        assignee = assignee
    )
}