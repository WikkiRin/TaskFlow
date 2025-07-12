package com.pet.taskflow.controller

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.TaskDto
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "Управление задачами в колонках")
@SecurityRequirement(name = "bearerAuth")
class TaskController(
    private val taskService: TaskService
) {
    private val log = LoggerFactory.getLogger(TaskController::class.java)

    @Operation(summary = "Создать задачу в колонке")
    @PostMapping
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskDto> {
        log.info("Создание задачи в колонке id=${request.columnId}")
        val task = taskService.createTask(request)
        log.info("Задача создана: id=${task.id}")
        return ResponseEntity.ok(task)
    }

    @Operation(summary = "Получить задачи по колонке")
    @GetMapping("/column/{columnId}")
    fun getTasks(@PathVariable columnId: Long): List<TaskDto> {
        log.info("Получение задач из колонки id=$columnId")
        return taskService.getTasksByColumn(columnId)
    }

    @Operation(summary = "Получить задачу по ID")
    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): TaskDto {
        log.info("Получение колонки по id=$id")
        return taskService.getTaskById(id)
    }

    @Operation(summary = "Обновить задачу по ID")
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTaskRequest
    ): TaskDto {
        log.info("Обновление задачи id=$id")
        return taskService.updateTask(id, request)
    }

    @Operation(summary = "Удалить задачу по ID")
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        log.info("Удаление задачи id=$id")
        taskService.deleteTask(id)
        log.info("Задача id=$id удалена")
        return ResponseEntity.noContent().build()
    }
}