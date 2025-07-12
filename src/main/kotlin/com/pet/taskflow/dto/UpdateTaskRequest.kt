package com.pet.taskflow.dto

import jakarta.validation.constraints.NotBlank

data class UpdateTaskRequest(
    @field:NotBlank(message = "Заголовок не может быть пустым")
    val title: String,
    val description: String? = "",
    val assigneeId: Long? = null,
    val position: Int? = null
)
