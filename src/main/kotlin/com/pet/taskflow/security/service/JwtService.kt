package com.pet.taskflow.security.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String
) {
    private val logger = LoggerFactory.getLogger(JwtService::class.java)
    private val jwtParser = Jwts.parserBuilder().setSigningKey(secretKey.toByteArray()).build()

    fun generateToken(username: String): String {
        logger.info("Генерация JWT токена для пользователя: {}", username)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24ч
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUsername(token: String): String {
        return try {
            val username = jwtParser.parseClaimsJws(token).body.subject
            logger.debug("Извлечен username из JWT: {}", username)
            username
        } catch (ex: Exception) {
            logger.warn("Ошибка при извлечении username из JWT: {}", ex.message)
            throw ex
        }
    }

    fun isTokenValid(token: String, username: String): Boolean {
        return try {
            val extractedUsername = extractUsername(token)
            val isValid = extractedUsername == username
            logger.debug("Валидация токена для {}: {}", username, isValid)
            isValid
        } catch (ex: Exception) {
            logger.warn("Ошибка при валидации токена для {}: {}", username, ex.message)
            false
        }
    }
}