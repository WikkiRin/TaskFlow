package com.pet.taskflow.exception

import java.time.LocalDateTime

data class ApiError(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String
)
