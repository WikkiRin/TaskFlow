package com.pet.taskflow.integration.config

import com.pet.taskflow.entity.User
import com.pet.taskflow.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Profile("test")
class TestDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostConstruct
    fun init() {
        if (!userRepository.existsByUsername("user")) {
            val user = User(
                username = "user",
                password = passwordEncoder.encode("password"),
                email = "user@mail.com"
            )
            userRepository.save(user)
        }
    }
}