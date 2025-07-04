package com.pet.taskflow.controller

import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.service.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
@Tag(name = "Board API", description = "Операции с досками")
@SecurityRequirement(name = "bearerAuth")
class BoardController(
    private val boardService: BoardService
) {
    private val log = LoggerFactory.getLogger(BoardController::class.java)

    @Operation(summary = "Создать новую доску")
    @PostMapping
    fun createBoard(
        @Valid @RequestBody request: BoardRequest,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<BoardDto> {
        log.info("Создание доски пользователем: ${user.username}, имя: '${request.title}'")
        val board = boardService.createBoard(request, user.username)
        log.info("Доска создана: id=${board.id}")
        return ResponseEntity.ok(board)
    }

    @Operation(summary = "Получить все доски текущего пользователя")
    @GetMapping
    fun getBoards(@AuthenticationPrincipal user: UserDetails): List<BoardDto> {
        log.info("Получение досок пользователя: ${user.username}")
        return boardService.getBoardsForUser(user.username)
    }

    @Operation(summary = "Получить доску по ID")
    @GetMapping("/{id}")
    fun getBoard(@PathVariable id: Long): BoardDto {
        log.info("Получение доски по id=$id")
        return boardService.getBoardById(id)
    }

    @Operation(summary = "Обновить доску по ID")
    @PutMapping("/{id}")
    fun updateBoard(
        @PathVariable id: Long,
        @Valid @RequestBody request: BoardRequest
    ): BoardDto {
        log.info("Обновление доски id=$id, новое имя='${request.title}'")
        return boardService.updateBoard(id, request)
    }

    @Operation(summary = "Удалить доску по ID")
    @DeleteMapping("/{id}")
    fun deleteBoard(@PathVariable id: Long): ResponseEntity<Void> {
        log.info("Удаление доски id=$id")
        boardService.deleteBoard(id)
        log.info("Доска id=$id успешно удалена")
        return ResponseEntity.noContent().build()
    }
}