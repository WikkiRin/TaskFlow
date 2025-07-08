package com.pet.taskflow.dto

import jakarta.validation.constraints.NotBlank

data class CreateColumnRequest(
    @field:NotBlank(message = "Название не может быть пустым")
    val name: String,
    val boardId: Long,
    val position: Int? = null
)
