package com.pet.taskflow.entity

import jakarta.persistence.*

@Entity
@Table(name = "board_column")
data class BoardColumn(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val position: Int,

    @ManyToOne
    val board: Board
)
