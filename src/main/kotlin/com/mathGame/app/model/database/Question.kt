package com.mathGame.app.model.database

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val expression: String,

    @Column(name = "correct_answer", nullable = false)
    val correctAnswer: Int,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) 