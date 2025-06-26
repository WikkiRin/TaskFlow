package com.pet.taskflow.dto

data class UpdateTaskRequest(
    val title: String,
    val description: String?,
    val assigneeId: Long?,
    val position: Int?
)
