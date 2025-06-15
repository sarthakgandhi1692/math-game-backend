package com.mathGame.app.model.database

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "leaderboard_entries")
data class LeaderboardEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "player_id", nullable = false)
    val playerId: UUID,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "total_score", nullable = false)
    var totalScore: Int = 0,

    @Column(name = "total_games", nullable = false)
    var totalGames: Int = 0
) {
    // Default constructor required by JPA
    constructor() : this(
        id = UUID.randomUUID(),
        playerId = UUID.randomUUID(),
        email = "",
        totalScore = 0,
        totalGames = 0
    )
} 