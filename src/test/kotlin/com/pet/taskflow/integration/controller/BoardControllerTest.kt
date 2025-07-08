package com.pet.taskflow.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.integration.config.BasePostgresContainer
import com.pet.taskflow.integration.config.TestSecurityConfig
import com.pet.taskflow.support.TestDataLoader
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class BoardControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val testDataLoader: TestDataLoader
) : BasePostgresContainer() {

    @BeforeEach
    fun setup() {
        testDataLoader.clearAll()
        testDataLoader.createUser("user")
    }

    @Test
    @WithMockUser(username = "user")
    fun `createBoard should create board and return 200`() {
        val request = BoardRequest(title = "Project G")

        mockMvc.perform(
            post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Project G"))
    }

    @Test
    @WithMockUser(username = "user")
    fun `createBoard should return 400 when title is blank`() {
        val request = BoardRequest(title = "  ")  // строка из пробелов

        mockMvc.perform(
            post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "user")
    fun `getBoards should return list of boards for current user`() {
        testDataLoader.createBoardsForUser("user", count = 4)

        val result = mockMvc.perform(get("/api/boards"))
            .andExpect(status().isOk)
            .andReturn()

        val content = result.response.contentAsString
        val boards: List<BoardDto> = objectMapper.readValue(content, object : TypeReference<List<BoardDto>>() {})

        assertTrue(boards.isNotEmpty(), "Список досок не должен быть пустым")
        boards.forEach { board ->
            assertNotNull(board.title)
        }
    }

    @Test
    @WithMockUser(username = "user")
    fun `getBoards should return empty list if user has no boards`() {
        testDataLoader.createUser("user")

        val result = mockMvc.perform(get("/api/boards"))
            .andExpect(status().isOk)
            .andReturn()

        val boards: List<BoardDto> = objectMapper.readValue(
            result.response.contentAsString,
            object : TypeReference<List<BoardDto>>() {})

        assertTrue(boards.isEmpty())
    }

    @Test
    @WithMockUser(username = "user")
    fun `getBoard should return board by id`() {
        val board = testDataLoader.createBoard(title = "Important Board", username = "user")

        val response = mockMvc.perform(get("/api/boards/${board.id}"))
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = response.response.contentAsString
        val result: BoardDto = objectMapper.readValue(responseBody, BoardDto::class.java)

        assertEquals(board.title, result.title)
        assertEquals(board.id, result.id)
        assertEquals(board.owner.id, result.ownerId)
    }

    @Test
    @WithMockUser(username = "user")
    fun `getBoard should return 404 when board not found`() {
        val nonexistentId = 99L

        mockMvc.perform(get("/api/boards/$nonexistentId"))
            .andExpect(status().isNotFound)
            .andExpect(
                jsonPath("$.message")
                    .value(containsString("Доска с id=$nonexistentId не найдена"))
            )
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateBoard should update board title by id`() {
        val board = testDataLoader.createBoard(title = "Old Title", username = "user")

        val updateRequest = BoardRequest(title = "Updated Title")

        val response = mockMvc.perform(
            put("/api/boards/${board.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = response.response.contentAsString
        val result: BoardDto = objectMapper.readValue(responseBody, BoardDto::class.java)

        assertEquals("Updated Title", result.title)
        assertEquals(board.id, result.id)
        assertEquals(board.owner.id, result.ownerId)
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateBoard should return 400 when title is blank`() {
        val board = testDataLoader.createBoard(title = "Valid", username = "user")

        val invalidRequest = BoardRequest(title = " ")

        mockMvc.perform(
            put("/api/boards/${board.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("не может быть пустым")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateBoard should return 404 when updating non-existent board`() {
        val nonexistentId = 9999L

        val updateRequest = BoardRequest(title = "New Title")

        mockMvc.perform(
            put("/api/boards/$nonexistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteBoard should delete board by id`() {
        val board = testDataLoader.createBoard(title = "To be deleted", username = "user")

        mockMvc.perform(delete("/api/boards/${board.id}"))
            .andExpect(status().isNoContent)

        // Проверка, что доска больше не возвращается
        mockMvc.perform(get("/api/boards/${board.id}"))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteBoard should return 404 when deleting non-existent board`() {
        val nonexistentId = 9999L

        mockMvc.perform(delete("/api/boards/$nonexistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }
}