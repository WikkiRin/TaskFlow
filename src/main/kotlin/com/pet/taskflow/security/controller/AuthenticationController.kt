package com.pet.taskflow.security.controller

import com.pet.taskflow.security.dto.LoginRequest
import com.pet.taskflow.security.dto.RegisterRequest
import com.pet.taskflow.security.service.JwtService
import com.pet.taskflow.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Авторизация")
class AuthenticationController(
    private val userService: UserService,
    private val jwtService: JwtService
) {
    private val logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<String> {
        logger.info("Регистрация нового пользователя: username={}, email={}", req.username, req.email)

        userService.register(req.username, req.email, req.password)

        logger.info("Пользователь успешно зарегистрирован: {}", req.username)
        return ResponseEntity.ok("User registered")
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Map<String, String>> {
        logger.info("Попытка входа пользователя: {}", req.username)

        val user = userService.validateCredentials(req.username, req.password)
            ?: run {
                logger.warn("Неудачная попытка входа: {}", req.username)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }

        val token = jwtService.generateToken(user.username)

        logger.info("Пользователь успешно вошел: {}", user.username)
        return ResponseEntity.ok(mapOf("token" to token))
    }
}