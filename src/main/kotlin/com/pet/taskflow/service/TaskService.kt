package com.pet.taskflow.service

import com.pet.taskflow.dto.CreateTaskRequest
import com.pet.taskflow.dto.TaskDto
import com.pet.taskflow.dto.UpdateTaskRequest
import com.pet.taskflow.entity.Task
import com.pet.taskflow.mapper.TaskMapper
import com.pet.taskflow.repository.TaskRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
    private val boardColumnService: BoardColumnService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(TaskService::class.java)

    fun createTask(request: CreateTaskRequest): TaskDto {
        val column = boardColumnService.findById(request.columnId)
        val assignee = request.assigneeId?.let { userService.findById(it) }
        val position = request.position ?: taskRepository.countByBoardColumnId(column.id).toInt()
        val task = taskMapper.toEntity(request, column, assignee, position)
        val saved = taskRepository.save(task)

        log.info("Создана задача id=${saved.id} в колонке id=${column.id}")
        return taskMapper.toDto(saved)
    }

    fun getTasksByColumn(columnId: Long): List<TaskDto> {
        val tasks = taskRepository.findAllByBoardColumnIdOrderByPosition(columnId)
        log.info("Получено ${tasks.size} задач в колонке id=$columnId")
        return tasks.map { taskMapper.toDto(it) }
    }

    fun findById(id: Long): Task {
        return taskRepository.findById(id).orElseThrow {
            EntityNotFoundException("Задача с id=$id не найдена")
        }
    }

    fun updateTask(id: Long, request: UpdateTaskRequest): TaskDto {
        val existing = findById(id)

        val assignee = request.assigneeId?.let { userService.findById(it) }

        val updated = taskMapper.updateEntity(existing, request, assignee)
        val saved = taskRepository.save(updated)

        log.info("Обновлена задача id=$id")
        return taskMapper.toDto(saved)
    }

    fun deleteTask(id: Long) {
        if (!taskRepository.existsById(id)) {
            throw EntityNotFoundException("Задача с id=$id не найдена")
        }
        taskRepository.deleteById(id)
        log.info("Задача id=$id удалена")
    }

}