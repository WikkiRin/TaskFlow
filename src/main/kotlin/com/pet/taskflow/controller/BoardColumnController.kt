package com.pet.taskflow.controller

import com.pet.taskflow.dto.BoardColumnDto
import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.service.BoardColumnService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/columns")
@Tag(name = "BoardColumn API", description = "Управление колонками досок")
@SecurityRequirement(name = "bearerAuth")
class BoardColumnController(
    private val columnService: BoardColumnService
) {
    private val log = LoggerFactory.getLogger(BoardColumnController::class.java)

    @Operation(summary = "Добавить колонку в доску")
    @PostMapping
    fun createColumn(@Valid @RequestBody request: CreateColumnRequest): ResponseEntity<BoardColumnDto> {
        log.info("Создание колонки '${request.name}' для доски id=${request.boardId}")
        val column = columnService.createColumn(request)
        log.info("Колонка создана: id=${column.id}")
        return ResponseEntity.ok(column)
    }

    @Operation(summary = "Получить все колонки доски")
    @GetMapping("/board/{boardId}")
    fun getColumns(@PathVariable boardId: Long): List<BoardColumnDto> {
        log.info("Получение всех колонок для доски id=$boardId")
        return columnService.getColumnsByBoard(boardId)
    }

    @Operation(summary = "Получить колонку по ID")
    @GetMapping("/{id}")
    fun getColumn(@PathVariable id: Long): BoardColumnDto {
        log.info("Получение колонки по id=$id")
        return columnService.getColumnById(id)
    }

    @Operation(summary = "Обновить колонку по ID")
    @PutMapping("/{id}")
    fun updateColumn(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateColumnRequest
    ): BoardColumnDto {
        log.info("Обновление колонки id=$id: новое имя='${request.name}', позиция=${request.position}")
        return columnService.updateColumn(id, request)
    }

    @Operation(summary = "Удалить колонку по ID")
    @DeleteMapping("/{id}")
    fun deleteColumn(@PathVariable id: Long): ResponseEntity<Void> {
        log.info("Удаление колонки id=$id")
        columnService.deleteColumn(id)
        log.info("Колонка id=$id удалена")
        return ResponseEntity.noContent().build()
    }
}