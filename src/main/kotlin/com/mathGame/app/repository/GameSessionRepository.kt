package com.mathGame.app.repository

import com.mathGame.app.model.database.GameSession
import com.mathGame.app.model.game.GameStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GameSessionRepository : JpaRepository<GameSession, UUID> {
    fun findByPlayer1IdOrPlayer2Id(player1Id: UUID, player2Id: UUID): List<GameSession>
    fun findByStatus(status: GameStatus): List<GameSession>
    fun findByRoomId(roomId: UUID): GameSession?
} 