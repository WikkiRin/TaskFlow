package com.pet.taskflow.security.service

import com.pet.taskflow.security.model.CustomUserDetails
import com.pet.taskflow.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userService: UserService
) : UserDetailsService {
    private val logger = LoggerFactory.getLogger(UserDetailsServiceImpl::class.java)

    override fun loadUserByUsername(username: String): CustomUserDetails {

        val user = try {
            userService.loadByUsername(username)
        } catch (ex: Exception) {
            logger.warn("Пользователь не найден: {}", username)
            throw UsernameNotFoundException("Пользователь не найден: $username")
        }

        return CustomUserDetails(
            id = user.id,
            usernameVal = user.username,
            passwordVal = user.password,
            role = listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}