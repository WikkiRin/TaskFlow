package com.pet.taskflow.security.service

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
): OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization") ?: run {
            logger.debug("Нет Authorization заголовка в запросе: ${request.requestURI}")
            return filterChain.doFilter(request, response)
        }

        if (!authHeader.startsWith("Bearer ")) {
            logger.debug("Заголовок Authorization не начинается с 'Bearer ': $authHeader")
            return filterChain.doFilter(request, response)
        }

        val token = authHeader.substring(7)
        val username = jwtService.extractUsername(token)

        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            logger.debug("Извлечен username из JWT: $username")

            val userDetails = userDetailsService.loadUserByUsername(username)

            if (jwtService.isTokenValid(token, userDetails.username)) {
                val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth

                logger.info("Аутентификация установлена для пользователя: $username")
            } else {
                logger.warn("Невалидный JWT токен для пользователя: $username")
            }
        }
        filterChain.doFilter(request, response)
    }
}