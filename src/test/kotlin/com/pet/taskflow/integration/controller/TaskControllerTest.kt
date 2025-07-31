package com.pet.taskflow.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.TaskDto
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.integration.config.BasePostgresContainer
import com.pet.taskflow.integration.config.TestSecurityConfig
import com.pet.taskflow.support.TestDataLoader
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class TaskControllerTest @Autowired constructor(
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
    fun `createTask should create task and return 200`() {
        val board = testDataLoader.createBoard(title = "Board A", username = "user")
        val column = testDataLoader.createColumn("To Do", board)

        val request = CreateTaskRequest(
            title = "New Task",
            columnId = column.id,
            description = "Task description",
            position = 0
        )

        val response = mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("New Task"))
            .andExpect(jsonPath("$.description").value("Task description"))
            .andExpect(jsonPath("$.columnId").value(column.id))
            .andReturn()

        val result: TaskDto = objectMapper.readValue(response.response.contentAsString, TaskDto::class.java)
        assertEquals("New Task", result.title)
        assertEquals("Task description", result.description)
        assertEquals(column.id, result.columnId)
    }

    @Test
    @WithMockUser(username = "user")
    fun `createTask should return 400 when title is blank`() {
        val board = testDataLoader.createBoard("Board A", "user")
        val column = testDataLoader.createColumn("To Do", board)
        val request = CreateTaskRequest(
            title = "   ",
            columnId = column.id
        )

        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("не может быть пустым")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `getTasks should return list of tasks for column`() {
        val board = testDataLoader.createBoard("Project Board", "user")
        val column = testDataLoader.createColumn("To Do", board)

        testDataLoader.createTasks(column, count = 4)

        val response = mockMvc.perform(
            get("/api/tasks/column/${column.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()

        val result: List<TaskDto> = objectMapper.readValue(
            response.response.contentAsString,
            object : TypeReference<List<TaskDto>>() {}
        )

        assertEquals(4, result.size)
        assertTrue(result.all { it.columnId == column.id })
        result.forEach { task ->
            assertNotNull(task.title)
        }
    }

    @Test
    @WithMockUser(username = "user")
    fun `getTasks should return 404 when column not found`() {
        val nonExistentColumnId = 9999L

        mockMvc.perform(
            get("/api/tasks/column/$nonExistentColumnId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `getTask should return task by id`() {
        val board = testDataLoader.createBoard(title = "Important Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Do", board = board, position = 0)
        val task = testDataLoader.createTask(title = "Task 1", boardColumn = column)

        val response = mockMvc.perform(get("/api/tasks/${task.id}"))
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = response.response.contentAsString
        val result: TaskDto = objectMapper.readValue(responseBody, TaskDto::class.java)

        assertEquals(task.id, result.id)
        assertEquals(task.title, result.title)
        assertEquals(task.boardColumn.id, result.columnId)
    }

    @Test
    @WithMockUser(username = "user")
    fun `getTask should return 404 when task not found`() {
        val nonexistentId = 9999L

        mockMvc.perform(get("/api/tasks/${nonexistentId}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateTask should update task and return 200`() {
        val board = testDataLoader.createBoard(title = "Test Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Do", board = board, position = 0)
        val task = testDataLoader.createTask(title = "Task 1", boardColumn = column, description = "Task description")

        val request = UpdateTaskRequest(title = "Update Task", position = 1)

        mockMvc.perform(
            put("/api/tasks/${task.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.title").value("Update Task"))
            .andExpect(jsonPath("$.position").value(1))
            .andExpect(jsonPath("$.description").value("Task description"))
            .andExpect(jsonPath("$.columnId").value(column.id))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateTask should return 404 when task not found`() {
        val request = UpdateTaskRequest(title = "Update Task", position = 1)
        val nonexistentId = 9999L

        mockMvc.perform(
            put("/api/tasks/${nonexistentId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `updateTask should return 400 when title is blank`() {
        val board = testDataLoader.createBoard(title = "Test Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Do", board = board, position = 0)
        val task = testDataLoader.createTask(title = "Task 1", boardColumn = column)

        val request = UpdateTaskRequest(title = "  ")
        mockMvc.perform(
            put("/api/tasks/${task.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("не может быть пустым")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteTask should delete column by id`() {
        val board = testDataLoader.createBoard(title = "Test Board", username = "user")
        val column = testDataLoader.createColumn(name = "To Do", board = board, position = 0)
        val task = testDataLoader.createTask(title = "Task 1", boardColumn = column)

        mockMvc.perform(delete("/api/tasks/${task.id}"))
            .andExpect(status().isNoContent)

        // Проверка, что задача больше не возвращается
        mockMvc.perform(get("/api/tasks/${task.id}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))
    }

    @Test
    @WithMockUser(username = "user")
    fun `deleteTask should return 404 when deleting non-existent task`() {
        val nonexistentId = 9999L

        mockMvc.perform(delete("/api/tasks/$nonexistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value(containsString("не найдена")))

    }
}