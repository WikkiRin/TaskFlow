package com.pet.taskflow.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ApiError> {
        logger.warn("IllegalArgumentException: {}", ex.message)
        return buildErrorResponse(ex.message ?: "Некорректные данные", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUserNotFound(ex: UsernameNotFoundException, request: WebRequest): ResponseEntity<ApiError> {
        logger.warn("UsernameNotFoundException: {}", ex.message)
        return buildErrorResponse(ex.message ?: "Пользователь не найден", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllOther(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
        logger.error("Unexpected exception occurred", ex)
        return buildErrorResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun buildErrorResponse(message: String, status: HttpStatus): ResponseEntity<ApiError> {
        val error = ApiError(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = message
        )
        return ResponseEntity(error, status)
    }
}