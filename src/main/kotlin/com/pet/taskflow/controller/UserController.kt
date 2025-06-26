package com.pet.taskflow.controller

import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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

    @Operation(summary = "Получить всех пользователей")
    @GetMapping
    fun getAll(): List<UserDto> = userService.getAll()
}