package com.pet.taskflow.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "board_column")
data class BoardColumn(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Название не может быть пустым")
    @Column(nullable = false)
    val name: String,
    val position: Int,

    @ManyToOne
    val board: Board
)
