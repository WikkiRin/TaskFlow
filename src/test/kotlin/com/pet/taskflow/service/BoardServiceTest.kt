package com.pet.taskflow.service

import com.pet.taskflow.dto.BoardDto
import com.pet.taskflow.dto.BoardRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.User
import com.pet.taskflow.mapper.BoardMapper
import com.pet.taskflow.repository.BoardRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.*
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoardServiceTest {

    @MockK
    lateinit var boardRepository: BoardRepository

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var boardMapper: BoardMapper

    @InjectMockKs
    lateinit var testObj: BoardService

    @BeforeAll
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @BeforeEach
    fun setup() {
        clearMocks(userService, boardRepository, boardMapper)
    }

    @Test
    fun `createBoard should create and return BoardDto`() {
        // GIVEN
        val request = BoardRequest(title = "Project R")
        val user = User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        val board = Board(id = 10L, title = request.title, owner = user)
        val dto = BoardDto(id = 10L, title = request.title, ownerId = user.id)

        // WHEN
        every { userService.loadByUsername(user.username) } returns user
        every { boardMapper.toEntity(request, user) } returns board
        every { boardRepository.save(board) } returns board
        every { boardMapper.toDto(board) } returns dto

        val result = testObj.createBoard(request, user.username)

        // THEN
        assert(result == dto)
        verifySequence {
            userService.loadByUsername(user.username)
            boardMapper.toEntity(request, user)
            boardRepository.save(board)
            boardMapper.toDto(board)
        }
    }

    @Test
    fun `getBoardsForUser should return list of BoardDto`() {
        // GIVEN
        val user = User(id = 2L, username = "user-2", email = "user-2@ex.com", password = "user-2-pass")
        val boards = listOf(
            Board(id = 12L, title = "Project T", owner = user),
            Board(id = 13L, title = "Project N", owner = user)
        )
        val dtos = listOf(
            BoardDto(id = 12L, title = "Project T", ownerId = user.id),
            BoardDto(id = 13L, title = "Project N", ownerId = user.id)
        )

        // WHEN
        every { userService.loadByUsername(user.username) } returns user
        every { boardRepository.findAllByOwnerId(user.id) } returns boards
        every { boardMapper.toDto(boards[0]) } returns dtos[0]
        every { boardMapper.toDto(boards[1]) } returns dtos[1]

        val result = testObj.getBoardsForUser(user.username)

        // THEN
        assert(result == dtos)
        verifySequence {
            userService.loadByUsername(user.username)
            boardRepository.findAllByOwnerId(user.id)
        }
        verify(exactly = 2) { boardMapper.toDto(any()) }
    }

    @Test
    fun `getBoardById should return BoardDto if exists`() {
        val user = User(id = 3L, username = "user-3", email = "user-3@ex.com", password = "user-3-pass")
        val board = Board(id = 10L, title = "Project J", owner = user)
        val dto = BoardDto(id = 10L, title = "Project J", ownerId = user.id)

        // создаём spyk на тестируемый объект
        val spyService = spyk(testObj)

        every { spyService.findById(10L) } returns board
        every { boardMapper.toDto(board) } returns dto

        // WHEN
        val result = spyService.getBoardById(10L)

        // THEN
        assert(result == dto)

        verify { spyService.findById(10L) }
        verify { boardMapper.toDto(board) }
    }

    @Test
    fun `findById should return Board if exists`() {
        // GIVEN
        val user = User(id = 4L, username = "user-4", email = "user-4@ex.com", password = "user-4-pass")
        val board = Board(id = 10L, title = "Project D", owner = user)

        every { boardRepository.findById(10L) } returns Optional.of(board)

        // WHEN
        val result = testObj.findById(10L)

        // THEN
        assert(result == board)
        verify { boardRepository.findById(10L) }

    }

    @Test
    fun `findById should throw if not found`() {
        // GIVEN
        val boardId = 9L
        every { boardRepository.findById(boardId) } returns Optional.empty()

        // WHEN
        val ex = assertThrows<EntityNotFoundException> {
            testObj.findById(boardId)
        }

        // THEN
        assert(ex.message == "Доска с id=$boardId не найдена")
    }

    @Test
    fun `updateBoard should update title and return BoardDto`() {
        // GIVEN
        val user = User(id = 5L, username = "user-5", email = "user-5@ex.com", password = "user-5-pass")
        val boardOld = Board(id = 10L, title = "Project S", owner = user)
        val request = BoardRequest(title = "Updated")
        val updated = boardOld.copy(title = request.title)
        val dto = BoardDto(id = 10L, title = "Updated", ownerId = user.id)
        val spyService = spyk(testObj)

        every { spyService.findById(10L) } returns boardOld
        every { boardRepository.save(updated) } returns updated
        every { boardMapper.toDto(updated) } returns dto

        // WHEN
        val result = spyService.updateBoard(10L, request)

        // THEN
        assert(result == dto)

        verify { spyService.findById(10L) }
        verify { boardRepository.save(updated) }
        verify { boardMapper.toDto(updated) }
    }

    @Test
    fun `deleteBoard should delete if exists`() {
        // GIVEN
        every { boardRepository.existsById(1L) } returns true
        every { boardRepository.deleteById(1L) } just Runs

        // WHEN
        testObj.deleteBoard(1L)

        // THEN
        verifySequence {
            boardRepository.existsById(1L)
            boardRepository.deleteById(1L) }

    }

    @Test
    fun `deleteBoard should throw if not exists`() {
        // GIVEN
        val id = 1L
        every { boardRepository.existsById(id) } returns false

        // WHEN
        val ex = assertThrows<EntityNotFoundException> {
            testObj.deleteBoard(id)
        }

        // THEN
        assert(ex.message == "Невозможно удалить: доска с id=$id не найдена")
    }
}