package com.mathGame.app.model.game

import com.mathGame.app.constants.PlayerConstants
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class GameStatus {
    WAITING,
    ACTIVE,
    COMPLETED
}

data class Player(
    val userId: String,
    val email: String,
    val name: String,
    var score: Int = PlayerConstants.INITIAL_SCORE,
    var correctAnswers: Int = PlayerConstants.INITIAL_CORRECT_ANSWERS
)

data class Question(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val correctAnswer: Int,
    val createdAt: Instant = Instant.now()
)

class GameRoom(
    val id: String = UUID.randomUUID().toString(),
    val player1: Player,
    var player2: Player? = null,
    var status: GameStatus = GameStatus.WAITING,
    var startTime: Instant? = null,
    var endTime: Instant? = null,
    val questions: MutableList<Question> = mutableListOf(),
    val playerAnswers: MutableMap<String, MutableMap<String, Int>> = ConcurrentHashMap()
) {
    fun addPlayer(player: Player): Boolean {
        if (player2 == null) {
            player2 = player
            return true
        }
        return false
    }

    fun hasPlayer(userId: String): Boolean {
        return player1.userId == userId || player2?.userId == userId
    }

    fun getOpponent(userId: String): Player? {
        return when (userId) {
            player1.userId -> player2
            player2?.userId -> player1
            else -> null
        }
    }

    fun addQuestion(question: Question) {
        questions.add(question)
    }

    fun recordAnswer(userId: String, questionId: String, answer: Int) {
        playerAnswers.computeIfAbsent(userId) { ConcurrentHashMap() }[questionId] = answer
    }

    fun isAnswerCorrect(userId: String, questionId: String): Boolean {
        val answer = playerAnswers[userId]?.get(questionId) ?: return false
        val question = questions.find { it.id == questionId } ?: return false
        return answer == question.correctAnswer
    }

    fun updateScore(userId: String, isCorrect: Boolean) {
        val player = when (userId) {
            player1.userId -> player1
            player2?.userId -> player2
            else -> return
        }
        
        if (isCorrect && player != null) {
            player.score += 1
            player.correctAnswers += 1
        }
    }

    fun getResult(userId: String): GameResult {
        val currentPlayer = when (userId) {
            player1.userId -> player1
            player2?.userId -> player2
            else -> return GameResult.DRAW
        } ?: return GameResult.DRAW
        
        val opponent = getOpponent(userId) ?: return GameResult.DRAW
        
        return when {
            currentPlayer.score > opponent.score -> GameResult.WIN
            currentPlayer.score < opponent.score -> GameResult.LOSE
            else -> GameResult.DRAW
        }
    }

    fun start() {
        if (player2 != null) {
            status = GameStatus.ACTIVE
            startTime = Instant.now()
        }
    }

    fun end() {
        status = GameStatus.COMPLETED
        endTime = Instant.now()
    }
}

enum class GameResult {
    WIN,
    LOSE,
    DRAW
} 