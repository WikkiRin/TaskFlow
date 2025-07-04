package com.pet.taskflow.service

import com.pet.taskflow.controller.TaskController
import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.entity.User
import com.pet.taskflow.exception.UserAlreadyExistsException
import com.pet.taskflow.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(TaskController::class.java)

    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            EntityNotFoundException("Пользователь с id=$id не найден")
        }.also {
            log.info("Найден пользователь id=$id")
        }
    }

    fun getAll(): List<UserDto> {
        return userRepository.findAll().map { user ->
            UserDto(
                id = user.id,
                username = user.username,
                email = user.email
            )
        }
    }

    fun loadByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Пользователь '$username' не найден")
    }

    fun register(username: String, email: String, rawPassword: String): User {
        if (userRepository.findByUsername(username) != null) {
            throw UserAlreadyExistsException("Имя пользователя уже занято")
        }
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val user = User(username = username, email = email, password = encodedPassword)
        return userRepository.save(user)
    }

    fun validateCredentials(username: String, rawPassword: String): User? {
        val user = userRepository.findByUsername(username) ?: return null
        return if (passwordEncoder.matches(rawPassword, user.password)) user else null
    }
}