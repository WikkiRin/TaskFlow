package com.pet.taskflow.service

import com.pet.taskflow.controller.TaskController
import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.entity.User
import com.pet.taskflow.exception.UserAlreadyExistsException
import com.pet.taskflow.mapper.UserMapper
import com.pet.taskflow.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {
    private val log = LoggerFactory.getLogger(TaskController::class.java)

    /**
     * Возвращает пользователя в виде DTO для отображения на клиенте.
     *
     * Используется в контроллерах и других местах, где нужна
     * безопасная и ограниченная информация о пользователе.
     *
     * @param id идентификатор пользователя
     * @return объект [UserDto], соответствующий указанному ID
     * @throws EntityNotFoundException если пользователь не найден
     */
    fun getUserById(id: Long): UserDto {
        val user = findById(id)
        log.info("Получен пользователь id=$user.id")
        return userMapper.toDto(user)
    }

    /**
     * Возвращает сущность пользователя для внутреннего использования.
     *
     * Применяется в сервисах, когда необходимо создать или связать
     * другие сущности, используя [User].
     *
     * @param id идентификатор пользователя
     * @return объект [User]
     * @throws EntityNotFoundException если пользователь не найден
     */
    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            EntityNotFoundException("Пользователь с id=$id не найден")
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