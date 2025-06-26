package com.pet.taskflow.dto

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val columnId: Long,
    val assigneeId: Long? = null,
    val position: Int? = null
)
