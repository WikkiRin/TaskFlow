package com.pet.taskflow.mapper

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.Task
import com.pet.taskflow.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDto(user: User): UserDto = UserDto(
        id = user.id,
        username = user.username,
        email = user.email
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
}