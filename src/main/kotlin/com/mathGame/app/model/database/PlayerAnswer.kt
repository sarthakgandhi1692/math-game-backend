package com.mathGame.app.model.database

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_answers")
data class PlayerAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "game_session_id", nullable = false)
    val gameSessionId: UUID,

    @Column(name = "player_id", nullable = false)
    val playerId: UUID,

    @Column(name = "question_id", nullable = false)
    val questionId: UUID,

    @Column(nullable = false)
    val answer: Int,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Column(name = "expression", nullable = false)
    val expression: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    // Default constructor required by JPA
    constructor() : this(
        id = UUID.randomUUID(),
        gameSessionId = UUID.randomUUID(),
        playerId = UUID.randomUUID(),
        questionId = UUID.randomUUID(),
        answer = 0,
        isCorrect = false,
        expression = "",
        createdAt = Instant.now()
    )
} 