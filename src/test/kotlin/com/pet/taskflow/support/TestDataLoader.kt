package com.pet.taskflow.support

import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.entity.User
import com.pet.taskflow.repository.BoardColumnRepository
import com.pet.taskflow.repository.BoardRepository
import com.pet.taskflow.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Вспомогательный класс для создания тестовых данных в интеграционных тестах.
 * Используется для генерации пользователей, досок и задач, связанных с ними.
 *
 * Пример использования в тестах:
 * ```
 * testDataLoader.createBoardsForUser("user", 3)
 * testDataLoader.createColumn("To do", board, 1)
 * ```
 */

@Component
class TestDataLoader(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
    private val boardColumnRepository: BoardColumnRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(
        username: String = "user",
        email: String = "user@example.com",
        rawPassword: String = "password"
    ): User {
        return userRepository.findByUsername(username) ?: userRepository.save(
            User(
                username = username,
                email = email,
                password = passwordEncoder.encode(rawPassword)
            )
        )
    }

    fun createBoard(
        title: String = "Test Board",
        username: String = "user"
    ): Board {
        val user = createUser(username)
        val board = Board(
            title = title,
            owner = user
        )
        return boardRepository.save(board)
    }

    fun createColumn(name: String, board: Board, position: Int = 0): BoardColumn {
        val column = BoardColumn(
            name = name,
            position = position,
            board = board
        )
        return boardColumnRepository.save(column)
    }

    fun createBoardsForUser(
        username: String = "user",
        count: Int = 3
    ): List<Board> {
        val user = createUser(username)
        val boards = (1..count).map {
            Board(title = "Board $it", owner = user)
        }
        return boardRepository.saveAll(boards)
    }

    fun clearAll() {
        boardRepository.deleteAll()
        boardColumnRepository.deleteAll()
        userRepository.deleteAll()
    }
}