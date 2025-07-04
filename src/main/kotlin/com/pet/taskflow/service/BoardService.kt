package com.pet.taskflow.service

import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.mapper.BoardMapper
import com.pet.taskflow.repository.BoardRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userService: UserService,
    private val boardMapper: BoardMapper
) {
    private val log = LoggerFactory.getLogger(BoardService::class.java)

    fun createBoard(request: BoardRequest, username: String): BoardDto {
        val user = userService.loadByUsername(username)
        val board = boardMapper.toEntity(request, user)
        boardRepository.save(board)

        log.info("Создана доска id=${board.id} пользователем '${username}'")
        return boardMapper.toDto(board)
    }

    fun getBoardsForUser(username: String): List<BoardDto> {
        log.info("Получение всех досок пользователя '$username'")
        val user = userService.loadByUsername(username)
        return boardRepository.findAllByOwnerId(user.id).map { boardMapper.toDto(it) }
    }

    /**
     * Возвращает доску в виде DTO для отображения на клиенте.
     *
     * Используется в контроллерах и других местах, где нужна
     * безопасная и ограниченная информация о доске.
     *
     * @param id идентификатор доски
     * @return объект [BoardDto], соответствующий указанному ID
     * @throws IllegalArgumentException если доска не найдена
     */
    fun getBoardById(id: Long): BoardDto {
        val board = findById(id)
        log.info("Получена доска id=$id")
        return boardMapper.toDto(board)
    }

    /**
     * Возвращает сущность доски для внутреннего использования.
     *
     * Применяется в сервисах, когда необходимо создать или связать
     * другие сущности (например, колонку), используя [Board].
     *
     * @param id идентификатор доски
     * @return объект [Board]
     * @throws EntityNotFoundException если доска не найдена
     */
    fun findById(id: Long): Board {
        return boardRepository.findById(id).orElseThrow {
            EntityNotFoundException("Доска с id=$id не найдена")
        }
    }

    fun updateBoard(id: Long, request: BoardRequest): BoardDto {
        val board = findById(id)
        val update = board.copy(title = request.title)
        boardRepository.save(update)
        log.info("Обновлена доска id=$id новым именем='${request.title}'")
        return boardMapper.toDto(update)
    }

    fun deleteBoard(id: Long) {
        if (!boardRepository.existsById(id)) {
            throw EntityNotFoundException("Невозможно удалить: доска с id=$id не найдена")
        }
        boardRepository.deleteById(id)
        log.info("Удалена доска id=$id")
    }
}