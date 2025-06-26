package com.pet.taskflow.dto

data class TaskDto(
    val id: Long,
    val title: String,
    val description: String?,
    val position: Int?,
    val columnId: Long,
    val assigneeId: Long?
)
