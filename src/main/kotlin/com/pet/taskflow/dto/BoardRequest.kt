package com.pet.taskflow.dto

import jakarta.validation.constraints.NotBlank

data class BoardRequest(
    @field:NotBlank(message = "Заголовок не может быть пустым")
    val title: String
)
