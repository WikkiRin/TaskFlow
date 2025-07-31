package com.pet.taskflow.service

import com.pet.taskflow.dto.UserDto
import com.pet.taskflow.entity.User
import com.pet.taskflow.exception.UserAlreadyExistsException
import com.pet.taskflow.mapper.UserMapper
import com.pet.taskflow.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var userMapper: UserMapper

    @InjectMockKs
    lateinit var testObj: UserService

    @BeforeAll
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @BeforeEach
    fun setup() {
        clearMocks(userRepository, passwordEncoder)
    }

    @Test
    fun `getUserById should return UserDto if exists`() {
        // GIVEN
        val user = User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")
        val dto = UserDto(id = 1L, username = "user-1", email = "user-1@ex.com")

        // создаём spyk на тестируемый объект
        val spyService = spyk(testObj)

        every { spyService.findById(1L) } returns user
        every { userMapper.toDto(user) } returns dto

        // WHEN
        val result = spyService.getUserById(1L)

        // THEN
        assert(result == dto)

        verify { spyService.findById(1L) }
        verify { userMapper.toDto(user) }
    }

    @Test
    fun `findById should return user if exists`() {
        // GIVEN
        val user = User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass")

        every { userRepository.findById(1L) } returns Optional.of(user)

        // WHEN
        val result = testObj.findById(1L)

        // THEN
        assertEquals(user, result)
        verify { userRepository.findById(1L) }
    }

    @Test
    fun `findById should throw if user not found`() {
        // GIVEN
        val id = 2L
        every { userRepository.findById(id) } returns Optional.empty()

        // WHEN
        val ex = assertThrows<EntityNotFoundException> {
            testObj.findById(id)
        }

        // THEN
        assertEquals("Пользователь с id=$id не найден", ex.message)
    }

    @Test
    fun `getAll should return list of UserDto`() {
        // GIVEN
        val users = listOf(
            User(id = 1L, username = "user-1", email = "user-1@ex.com", password = "user-1-pass"),
            User(id = 2L, username = "user-2", email = "user-2@ex.com", password = "user-2-pass")
        )
        val dtos = listOf(
            UserDto(id = 1L, username = "user-1", email = "user-1@ex.com"),
            UserDto(id = 2L, username = "user-2", email = "user-2@ex.com")
        )

        every { userRepository.findAll() } returns users

        // WHEN
        val result = testObj.getAll()

        // THEN
        assertEquals(dtos, result)

        verify { userRepository.findAll() }
    }

    @Test
    fun `loadByUsername should return user if found`() {
        // GIVEN
        val user = User(id = 3L, username = "user-3", email = "user-3@ex.com", password = "user-3")

        every { userRepository.findByUsername("user-3") } returns user

        // WHEN
        val result = testObj.loadByUsername("user-3")

        // THEN
        assertEquals(user, result)

        verify { userRepository.findByUsername("user-3") }

    }

    @Test
    fun `loadByUsername should throw if not found`() {
        // GIVEN
        val username = "user"
        every { userRepository.findByUsername(username) } returns null

        // WHEN
        val ex = assertThrows<UsernameNotFoundException> {
            testObj.loadByUsername(username)
        }

        // THEN
        assertEquals("Пользователь '$username' не найден", ex.message)

        verify { userRepository.findByUsername(username) }

    }

    @Test
    fun `register should save and return new user`() {
        // GIVEN
        val username = "user-4"
        val email = "user-4@ex.com"
        val rawPassword = "user-4-pass"
        val encoded = "encoded-123"

        val user = User(username = username, email = email, password = encoded)
        val save = User(id = 99L, username = username, email = email, password = encoded)

        every { userRepository.findByUsername(username) } returns null
        every { passwordEncoder.encode(rawPassword) } returns encoded
        every { userRepository.save(user) } returns save

        // WHEN
        val result = testObj.register(username, email, rawPassword)

        // THEN
        assertEquals(99L, result.id)
        assertEquals(username, result.username)
        assertEquals(email, result.email)
        assertEquals(encoded, result.password)

        verifySequence {
            userRepository.findByUsername(username)
            passwordEncoder.encode(rawPassword)
            userRepository.save(any())
        }
    }

    @Test
    fun `register should throw if username is taken`() {
        // GIVEN
        val existing = User(id = 4L, username = "user-4", email = "user-4@ex.com", password = "user-4-pass")
        every { userRepository.findByUsername("taken") } returns existing

        // WHEN
        val ex = assertThrows<UserAlreadyExistsException> {
            testObj.register("taken", "taken@mail.com", "password")
        }

        // THEN
        assertEquals("Имя пользователя уже занято", ex.message)

        verify { userRepository.findByUsername("taken") }

    }

    @Test
    fun `validateCredentials should return user if password matches`() {
        // GIVEN
        val user = User(id = 5L, username = "user-5", email = "user-5@ex.com", password = "user-5-pass")

        every { userRepository.findByUsername("user-5") } returns user
        every { passwordEncoder.matches("plain", "user-5-pass") } returns true

        // WHEN
        val result = testObj.validateCredentials("user-5", "plain")

        // THEN
        assertEquals(user, result)

        verify { userRepository.findByUsername("user-5") }
        verify { passwordEncoder.matches("plain", "user-5-pass") }
    }

    @Test
    fun `validateCredentials should return null if user not found`() {
        // GIVEN
        every { userRepository.findByUsername("user") } returns null

        // WHEN
        val result = testObj.validateCredentials("user", "any")

        // THEN
        assertNull(result)
    }

    @Test
    fun `validateCredentials should return null if password does not match`() {
        // GIVEN
        val user = User(id = 5L, username = "user-5", email = "user-5@ex.com", password = "user-5-pass")

        every { userRepository.findByUsername("user-5") } returns user
        every { passwordEncoder.matches("wrong", "user-5-pass") } returns false

        // WHEN
        val result = testObj.validateCredentials("user-5", "wrong")

        // THEN
        assertNull(result)
    }
}