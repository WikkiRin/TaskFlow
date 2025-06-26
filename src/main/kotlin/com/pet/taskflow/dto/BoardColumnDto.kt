package com.pet.taskflow.dto

data class BoardColumnDto (
    val id: Long,
    val name: String,
    val position: Int?,
    val boardId: Long
)