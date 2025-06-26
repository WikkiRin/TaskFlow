package com.pet.taskflow.dto

data class CreateColumnRequest(
    val name: String,
    val boardId: Long,
    val position: Int? = null
)
