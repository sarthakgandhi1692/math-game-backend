package com.mathGame.app.model.database

import com.mathGame.app.model.game.GameStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "game_sessions")
data class GameSession(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "room_id", nullable = false)
    val roomId: UUID,

    @Column(name = "player1_id", nullable = false)
    val player1Id: UUID,

    @Column(name = "player2_id")
    val player2Id: UUID? = null,

    @Column(name = "start_time")
    val startTime: Instant? = null,

    @Column(name = "end_time")
    val endTime: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: GameStatus
) {
    // Default constructor required by JPA
    constructor() : this(
        id = UUID.randomUUID(),
        roomId = UUID.randomUUID(),
        player1Id = UUID.randomUUID(),
        player2Id = null,
        startTime = null,
        endTime = null,
        status = GameStatus.WAITING
    )
} 