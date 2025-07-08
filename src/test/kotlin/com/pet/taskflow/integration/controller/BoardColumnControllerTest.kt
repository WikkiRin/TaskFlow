package com.pet.taskflow.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.taskflow.dto.BoardColumnDto
import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.integration.config.BasePostgresContainer
import com.pet.taskflow.integration.config.TestSecurityConfig
import com.pet.taskflow.support.TestDataLoader
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class BoardColumnControllerTest @Autowired constructor(
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
    fun `createColumn should create column and return 200`() {
        val board = testDataLoader.createBoard(title = "Board A", username = "user")
        val request = CreateColumnRequest(name = "To Do", boardId = board.id)

        val response = mockMvc.perform(
            post("/api/columns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("To Do"))
            .andExpect(jsonPath("$.boardId").value(board.id))
            .andReturn()

        val result: BoardColumnDto = objectMapper.readValue(
            response.response.contentAsString,
            BoardColumnDto::class.java
        )
        assertEquals("To Do", result.name)
        assertEquals(board.id, result.boardId)
    }

    @Test
    @WithMockUser(username = "user")
    fun `createColumn should return 400 when column name is blank`() {
        val board = testDataLoader.createBoard(title = "Board A", username = "user")
        val request = CreateColumnRequest(name = " ", boardId = board.id)

        mockMvc.perform(
            post("/api/columns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("не может быть пустым")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `getColumns should return all columns for given board`() {
        val board = testDataLoader.createBoard(title = "Project J", username = "user")
        testDataLoader.createColumn(name = "To Do", board = board, position = 0)
        testDataLoader.createColumn(name = "In Progress", board = board, position = 1)
        testDataLoader.createColumn(name = "Done", board = board, position = 2)

        val response = mockMvc.perform(get("/api/columns/board/${board.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].name").value("To Do"))
            .andExpect(jsonPath("$[1].name").value("In Progress"))
            .andExpect(jsonPath("$[2].name").value("Done"))
            .andReturn()

        val responseBody = response.response.contentAsString
        val columns: List<BoardColumnDto> = objectMapper.readValue(responseBody, object : TypeReference<List<BoardColumnDto>>() {})

        assertEquals(3, columns.size)
        assertTrue(columns.all { it.boardId == board.id })
    }

    @Test
    @WithMockUser(username = "user")
    fun `getColumns should return 404 when board does not exist`() {
        val nonexistentBoardId = 9999L

        mockMvc.perform(get("/api/columns/board/$nonexistentBoardId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateColumn should update column and return 200`() {
        val board = testDataLoader.createBoard(title = "Test Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Do", board = board, position = 0)

        val request = UpdateColumnRequest(
            name = "In Progress",
            position = 1
        )

        mockMvc.perform(
            put("/api/columns/${column.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(column.id))
            .andExpect(jsonPath("$.name").value("In Progress"))
            .andExpect(jsonPath("$.position").value(1))
            .andExpect(jsonPath("$.boardId").value(board.id))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateColumn should return 404 when column not found`() {
        val request = UpdateColumnRequest(name = "Updated", position = 2)
        val nonexistentId = 9999L

        mockMvc.perform(
            put("/api/columns/$nonexistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateColumn should return 400 when name is blank`() {
        val board = testDataLoader.createBoard("Board", "user")
        val column = testDataLoader.createColumn("Column", board, position = 0)

        val request = UpdateColumnRequest(name = " ")

        mockMvc.perform(
            put("/api/columns/${column.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("не может быть пустым")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateColumn should return 404 when updating non-existent column`() {
        val request = UpdateColumnRequest(
            name = "Updated Name",
            position = 1
        )

        mockMvc.perform(
            put("/api/columns/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteColumn should delete column by id `() {
        val board = testDataLoader.createBoard(title = "Test Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Delete", board = board)

        mockMvc.perform(delete("/api/columns/${column.id}"))
            .andExpect(status().isNoContent)

        // Проверка, что колонка больше не возвращается
        mockMvc.perform(get("/api/columns/${column.id}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteColumn should return 404 when deleting non-existent column`() {
        val nonexistentId = 9999L

        mockMvc.perform(delete("/api/columns/$nonexistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }
}