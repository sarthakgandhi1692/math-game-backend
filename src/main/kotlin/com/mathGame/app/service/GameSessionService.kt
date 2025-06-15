package com.mathGame.app.service

import com.mathGame.app.model.database.GameSession
import com.mathGame.app.model.game.GameStatus
import com.mathGame.app.repository.GameSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class GameSessionService(
    private val gameSessionRepository: GameSessionRepository
) {
    @Transactional
    fun createGameSession(roomId: UUID, player1Id: UUID, player2Id: UUID? = null): GameSession {
        val session = GameSession(
            roomId = roomId,
            player1Id = player1Id,
            player2Id = player2Id,
            status = GameStatus.WAITING
        )
        return gameSessionRepository.save(session)
    }

    @Transactional
    fun startGameSession(sessionId: UUID): GameSession {
        val session = gameSessionRepository.findById(sessionId).orElseThrow()
        return gameSessionRepository.save(session.copy(
            startTime = Instant.now(),
            status = GameStatus.ACTIVE
        ))
    }

    @Transactional
    fun endGameSession(sessionId: UUID): GameSession {
        val session = gameSessionRepository.findById(sessionId).orElseThrow()
        return gameSessionRepository.save(session.copy(
            endTime = Instant.now(),
            status = GameStatus.COMPLETED
        ))
    }

    fun getActiveSessions(): List<GameSession> {
        return gameSessionRepository.findByStatus(GameStatus.ACTIVE)
    }

    fun getPlayerSessions(playerId: UUID): List<GameSession> {
        return gameSessionRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId)
    }

    fun getSessionByRoomId(roomId: UUID): GameSession? {
        return gameSessionRepository.findByRoomId(roomId)
    }
} 