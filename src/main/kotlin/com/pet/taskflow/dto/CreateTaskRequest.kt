package com.pet.taskflow.dto

import jakarta.validation.constraints.NotBlank

data class CreateTaskRequest(
    @field:NotBlank(message = "Заголовок не может быть пустым")
    val title: String,
    val description: String? = "",
    val columnId: Long,
    val assigneeId: Long? = null,
    val position: Int? = null
)
