package com.pet.taskflow.service

import com.pet.taskflow.dto.BoardColumnDto
import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.mapper.BoardColumnMapper
import com.pet.taskflow.repository.BoardColumnRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BoardColumnService(
    private val boardService: BoardService,
    private val columnRepository: BoardColumnRepository,
    private val columnMapper: BoardColumnMapper
) {
    private val log = LoggerFactory.getLogger(BoardColumnService::class.java)

    fun createColumn(request: CreateColumnRequest): BoardColumnDto {
        val board = boardService.findById(request.boardId)

        val position = request.position ?: columnRepository.findAllByBoardIdOrderByPosition(board.id).size

        val column = columnMapper.toEntity(request, board, position)
        val saved = columnRepository.save(column)

        log.info("Создана колонка id=${saved.id} для доски id=${board.id} на позиции $position")
        return columnMapper.toDto(saved)
    }

    fun getColumnsByBoard(boardId: Long): List<BoardColumnDto> {
        val board = boardService.findById(boardId)
        val columns = columnRepository.findAllByBoardIdOrderByPosition(board.id)
        log.info("Получено ${columns.size} колонок для доски id=$board.id")
        return columns.map { columnMapper.toDto(it) }
    }

    /**
     * Возвращает колонку в виде DTO для отображения на клиенте.
     *
     * Используется в контроллерах и других местах, где нужна
     * безопасная и ограниченная информация о колонке.
     *
     * @param id идентификатор колонки
     * @return объект [BoardColumnDto], соответствующий указанному ID
     * @throws EntityNotFoundException если колонка не найдена
     */
    fun getColumnById(id: Long): BoardColumnDto {
        val column = findById(id)
        log.info("Получена колонка id=$id")
        return columnMapper.toDto(column)
    }

    /**
     * Возвращает сущность колонки для внутреннего использования.
     *
     * Применяется в сервисах, когда необходимо создать или связать
     * другие сущности (например, задачу), используя [BoardColumn].
     *
     * @param id идентификатор колонки
     * @return объект [BoardColumn]
     * @throws EntityNotFoundException если колонка не найдена
     */
    fun findById(id: Long): BoardColumn {
        return columnRepository.findById(id).orElseThrow {
            EntityNotFoundException("Колонка с id=$id не найдена")
        }
    }

    fun updateColumn(id: Long, request: UpdateColumnRequest): BoardColumnDto {
        val existing = findById(id)
        val updated = columnMapper.updateEntity(existing, request)
        val saved = columnRepository.save(updated)

        log.info("Обновлена колонка id=$id: новое имя='${saved.name}', позиция=${saved.position}")
        return columnMapper.toDto(saved)
    }

    fun deleteColumn(id: Long) {
        if (!columnRepository.existsById(id)) {
            throw EntityNotFoundException("Колонка с id=$id не найдена")
        }
        columnRepository.deleteById(id)
        log.info("Удалена колонка id=$id")
    }
}