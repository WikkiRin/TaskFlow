package com.pet.taskflow.integration.security.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.taskflow.support.TestDataLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val testDataLoader: TestDataLoader
) {

    @BeforeEach
    fun setup() {
        testDataLoader.clearAll()
    }

    @Test
    fun `should register, login and access protected endpoint`() {
        val registerRequest = mapOf(
            "username" to "testuser",
            "email" to "test@example.com",
            "password" to "password123"
        )

        // Регистрация
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)

        val loginRequest = mapOf(
            "username" to "testuser",
            "password" to "password123"
        )

        // Логин и получение токена
        val loginResponse = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
            .andReturn()

        val token = objectMapper.readTree(loginResponse.response.contentAsString)["token"].asText()

        // Доступ к защищённому эндпоинту
        mockMvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andReturn()
    }
}