package com.pet.taskflow.dto

data class UpdateColumnRequest(
    val name: String,
    val position: Int? = null
)
