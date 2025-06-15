package com.mathGame.app.repository

import com.mathGame.app.model.database.PlayerAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PlayerAnswerRepository : JpaRepository<PlayerAnswer, UUID> {
    fun findByGameSessionId(gameSessionId: UUID): List<PlayerAnswer>
    fun findByPlayerId(playerId: UUID): List<PlayerAnswer>
    fun findByGameSessionIdAndPlayerId(gameSessionId: UUID, playerId: UUID): List<PlayerAnswer>
} 