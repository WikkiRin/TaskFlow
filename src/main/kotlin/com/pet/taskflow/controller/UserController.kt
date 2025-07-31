package com.pet.taskflow.controller

import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.security.model.CustomUserDetails
import com.pet.taskflow.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User API", description = "Управление пользователями")
class UserController(
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(TaskController::class.java)

    @Operation(summary = "Получить информацию о себе")
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<UserDto> {
        log.info("Получение информации о пользователе по id=${user.id}")
        val userDto = userService.getUserById(user.id)
        return ResponseEntity.ok(userDto)
    }
}