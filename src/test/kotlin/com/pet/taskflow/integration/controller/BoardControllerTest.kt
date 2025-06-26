package com.pet.taskflow.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.integration.config.BasePostgresContainer
import com.pet.taskflow.integration.config.TestSecurityConfig
import com.pet.taskflow.repository.BoardRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class BoardControllerTest : BasePostgresContainer() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Test
    @WithMockUser(username = "user")
    fun `should create board and return 200`() {
        val request = BoardRequest(title = "Project G")

        mockMvc.perform(
            post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Project G"))
    }
}