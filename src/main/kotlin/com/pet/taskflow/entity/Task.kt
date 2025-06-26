package com.pet.taskflow.entity

import jakarta.persistence.*

@Entity
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val position: Int? = null,

    @ManyToOne
    val boardColumn: BoardColumn,

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    val assignee: User? = null
)
