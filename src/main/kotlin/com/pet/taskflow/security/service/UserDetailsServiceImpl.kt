package com.pet.taskflow.security.service

import com.pet.taskflow.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userService: UserService
) : UserDetailsService {
    private val logger = LoggerFactory.getLogger(UserDetailsServiceImpl::class.java)

    override fun loadUserByUsername(username: String): UserDetails {

        val user = try {
            userService.loadByUsername(username)
        } catch (ex: Exception) {
            logger.warn("Пользователь не найден: {}", username)
            throw UsernameNotFoundException("Пользователь не найден: $username")
        }

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}