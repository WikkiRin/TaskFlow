package com.pet.taskflow.security.config

import com.pet.taskflow.security.service.JwtAuthFilter
import com.pet.taskflow.security.service.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class AppConfig{

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtAuthFilter(jwtService: JwtService, userDetailsService: UserDetailsService): JwtAuthFilter {
        return JwtAuthFilter(jwtService, userDetailsService)
    }
}