package com.pet.taskflow.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Название не может быть пустым")
    @Column(nullable = false)
    val title: String,

    val description: String? = null,

    val position: Int? = null,

    @ManyToOne
    @JoinColumn(name = "column_id")
    val boardColumn: BoardColumn,

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    val assignee: User? = null
)
