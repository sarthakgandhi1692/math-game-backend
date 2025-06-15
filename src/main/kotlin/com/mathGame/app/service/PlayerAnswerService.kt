package com.mathGame.app.service

import com.mathGame.app.model.database.PlayerAnswer
import com.mathGame.app.repository.PlayerAnswerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PlayerAnswerService(
    private val playerAnswerRepository: PlayerAnswerRepository
) {
    @Transactional
    fun saveAnswer(
        gameSessionId: UUID,
        playerId: UUID,
        questionId: UUID,
        answer: Int,
        isCorrect: Boolean,
        expression: String
    ): PlayerAnswer {
        val playerAnswer = PlayerAnswer(
            gameSessionId = gameSessionId,
            playerId = playerId,
            questionId = questionId,
            answer = answer,
            isCorrect = isCorrect,
            expression = expression
        )
        return playerAnswerRepository.save(playerAnswer)
    }

    @Transactional
    fun saveAnswers(answers: List<PlayerAnswer>): List<PlayerAnswer> {
        return playerAnswerRepository.saveAll(answers)
    }

    fun getSessionAnswers(gameSessionId: UUID): List<PlayerAnswer> {
        return playerAnswerRepository.findByGameSessionId(gameSessionId)
    }

    fun getPlayerAnswers(playerId: UUID): List<PlayerAnswer> {
        return playerAnswerRepository.findByPlayerId(playerId)
    }

    fun getPlayerSessionAnswers(gameSessionId: UUID, playerId: UUID): List<PlayerAnswer> {
        return playerAnswerRepository.findByGameSessionIdAndPlayerId(gameSessionId, playerId)
    }

    fun getPlayerScore(playerId: UUID): Int {
        return playerAnswerRepository.findByPlayerId(playerId)
            .count { it.isCorrect }
    }
} 