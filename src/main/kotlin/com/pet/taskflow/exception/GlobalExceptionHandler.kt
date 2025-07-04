package com.pet.taskflow.exception

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiError> {
        logger.warn("IllegalArgumentException: {}", ex.message)
        return buildErrorResponse(ex.message ?: "Некорректные данные", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors
            .joinToString(separator = "; ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation failed: {}", errors)
        return buildErrorResponse("Ошибка валидации: $errors", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiError> {
        val errors = ex.constraintViolations
            .joinToString(separator = "; ") { violation ->
                val path = violation.propertyPath.toString()
                "$path: ${violation.message}"
            }
        logger.warn("Constraint violations: {}", errors)
        return buildErrorResponse("Ошибка валидации: $errors", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUserNotFound(ex: UsernameNotFoundException): ResponseEntity<ApiError> {
        logger.warn("UsernameNotFoundException: {}", ex.message)
        return buildErrorResponse(ex.message ?: "Пользователь не найден", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException): ResponseEntity<ApiError> {
        logger.warn("EntityNotFoundException: {}", ex.message)
        return buildErrorResponse(ex.message ?: "Ресурс не найден", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ResponseEntity<ApiError> {
        return buildErrorResponse(ex.message ?: "Пользователь уже существует", HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllOther(ex: Exception): ResponseEntity<ApiError> {
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