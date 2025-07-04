package com.pet.taskflow.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
data class Board(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Заголовок не может быть пустым")
    @Column(nullable = false)
    val title: String,

    @ManyToOne
    val owner: User
)
