package com.pet.taskflow.service

import com.pet.taskflow.dto.BoardColumnDto
import com.pet.taskflow.dto.CreateColumnRequest
import com.pet.taskflow.dto.UpdateColumnRequest
import com.pet.taskflow.entity.Board
import com.pet.taskflow.entity.BoardColumn
import com.pet.taskflow.mapper.BoardColumnMapper
import com.pet.taskflow.repository.BoardColumnRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.*
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoardColumnServiceTest {

    @MockK
    lateinit var columnRepository: BoardColumnRepository

    @MockK
    lateinit var boardService: BoardService

    @MockK
    lateinit var columnMapper: BoardColumnMapper

    @InjectMockKs
    lateinit var testObj: BoardColumnService

    @BeforeAll
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @BeforeEach
    fun setup() {
        clearMocks(columnRepository, boardService, columnMapper)
    }

    @Test
    fun `createColumn should create and return BoardColumnDto`() {
        // GIVEN
        val request = CreateColumnRequest(boardId = 1L, name = "In progress", position = null)
        val board = Board(id = 1L, title = "Project S", owner = mockk())
        val column = BoardColumn(id = 2L, name = "Inbox", board = board, position = 0)
        val dto = BoardColumnDto(id = 2L, name = "Inbox", position = 0, boardId = board.id)

        every { boardService.findById(1L) } returns board
        every { columnRepository.findAllByBoardIdOrderByPosition(1L) } returns emptyList()
        every { columnMapper.toEntity(request, board, 0) } returns column
        every { columnRepository.save(column) } returns column
        every { columnMapper.toDto(column) } returns dto

        // WHEN
        val result = testObj.createColumn(request)

        // THEN
        assert(result == dto)

        verifySequence {
            boardService.findById(1L)
            columnRepository.findAllByBoardIdOrderByPosition(1L)
            columnMapper.toEntity(request, board, 0)
            columnRepository.save(column)
            columnMapper.toDto(column)
        }
    }

    @Test
    fun `createColumn should create and return BoardColumnDto if position is not null`() {
        // GIVEN
        val request = CreateColumnRequest(boardId = 2L, name = "Done", position = 1)
        val board = Board(id = 2L, title = "Project G", owner = mockk())
        val column = BoardColumn(id = 10L, name = "Done", board = board, position = 1)
        val dto = BoardColumnDto(id = 10L, name = "Done", position = 1, boardId = board.id)

        every { boardService.findById(2L) } returns board
        every { columnMapper.toEntity(request, board, 1) } returns column
        every { columnRepository.save(column) } returns column
        every { columnMapper.toDto(column) } returns dto

        // WHEN
        val result = testObj.createColumn(request)

        // THEN
        assert(result == dto)

        verifySequence {
            boardService.findById(2L)
            columnMapper.toEntity(request, board, 1)
            columnRepository.save(column)
            columnMapper.toDto(column)
        }

        verify(exactly = 0) { columnRepository.findAllByBoardIdOrderByPosition(any()) }
    }

    @Test
    fun `getColumnsByBoard should return list of BoardColumnDto`() {
        // GIVEN
        val board = Board(id = 3L, title = "Project H", owner = mockk())
        val columns = listOf(
            BoardColumn(id = 1L, name = "To Do", board = board, position = 0),
            BoardColumn(id = 2L, name = "In Progress", board = board, position = 1)
        )
        val dtos = listOf(
            BoardColumnDto(id = 1L, name = "To Do", position = 0, boardId = board.id),
            BoardColumnDto(id = 2L, name = "In Progress", position = 1, boardId = board.id)
        )

        every { columnRepository.findAllByBoardIdOrderByPosition(3L) } returns columns
        every { columnMapper.toDto(columns[0]) } returns dtos[0]
        every { columnMapper.toDto(columns[1]) } returns dtos[1]

        // WHEN
        val result = testObj.getColumnsByBoard(3L)

        // THEN
        assert(result == dtos)

        verifySequence {
            columnRepository.findAllByBoardIdOrderByPosition(3L)
            columnMapper.toDto(columns[0])
            columnMapper.toDto(columns[1])
        }
        verify(exactly = 2) { columnMapper.toDto(any()) }
    }

    @Test
    fun `findById should return BoardColumn if exists`() {
        // GIVEN
        val column = mockk<BoardColumn>()

        every { columnRepository.findById(10L) } returns Optional.of(column)

        // WHEN
        val result = testObj.findById(10L)

        // THEN
        assert(result == column)
        verify { columnRepository.findById(10L) }
    }

    @Test
    fun `findById should throw if not found`() {
        // GIVEN
        val id = 5L
        every { columnRepository.findById(id) } returns Optional.empty()

        // WHEN
        val ex = assertThrows<IllegalArgumentException> {
            testObj.findById(id)
        }

        // THEN
        assert(ex.message == "Колонка с id=$id не найдена")
    }

    @Test
    fun `updateColumn should update and return BoardColumnDto`() {
        // GIVEN
        val board = Board(id = 4L, title = "Project E", owner = mockk())
        val oldColumn = BoardColumn(id = 6L, name = "Old", board = board, position = 0)
        val request = UpdateColumnRequest(name = "Updated", position = 1)
        val updatedColumn = oldColumn.copy(name = "Updated", position = 1)
        val dto = BoardColumnDto(id = 2L, name = "Updated", position = 1, boardId = board.id)
        val service = spyk(testObj)

        every { service.findById(6L) } returns oldColumn
        every { columnMapper.updateEntity(oldColumn, request) } returns updatedColumn
        every { columnRepository.save(updatedColumn) } returns updatedColumn
        every { columnMapper.toDto(updatedColumn) } returns dto

        // WHEN
        val result = service.updateColumn(6L, request)

        // THEN
        assert(result == dto)

        verify { service.findById(6L) }
        verify { columnMapper.updateEntity(oldColumn, request) }
        verify { columnRepository.save(updatedColumn) }
        verify { columnMapper.toDto(updatedColumn) }
    }

    @Test
    fun `deleteColumn should delete if exists`() {
        // GIVEN
        every { columnRepository.existsById(3L) } returns true
        every { columnRepository.deleteById(3L) } just Runs

        // WHEN
        testObj.deleteColumn(3L)

        // THEN
        verifySequence {
            columnRepository.existsById(3L)
            columnRepository.deleteById(3L) }
    }

    @Test
    fun `deleteColumn should throw if not exists`() {
        // GIVEN
        val id = 4L
        every { columnRepository.existsById(id) } returns false

        // WHEN
        val ex = assertThrows<IllegalArgumentException> {
            testObj.deleteColumn(id)
        }

        // THEN
        assert(ex.message == "Колонка с id=$id не найдена")
    }
}