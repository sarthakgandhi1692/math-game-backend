package com.mathGame.app.controller

import com.mathGame.app.model.game.Player
import com.mathGame.app.service.GameManager
import com.mathGame.app.service.PlayerAnswerService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/test/game")
class GameTestController(
    private val gameService: GameManager,
    private val playerAnswerService: PlayerAnswerService
) {
    @PostMapping("/start")
    fun startTestGame(): Map<String, Any?> {
        // Create two test players
        val player1 = Player(
            userId = UUID.randomUUID().toString(),
            email = "player1@test.com",
            name = "Player 1"
        )
        
        val player2 = Player(
            userId = UUID.randomUUID().toString(),
            email = "player2@test.com",
            name = "Player 2"
        )

        // Add players to waiting room
        val room1 = gameService.addPlayerToWaitingRoom(player1)
        val room2 = gameService.addPlayerToWaitingRoom(player2)

        // Start the game if room was created
        if (room2 != null) {
            gameService.startGame(room2.id)
        }

        return mapOf(
            "player1" to player1,
            "player2" to player2,
            "room" to room2
        )
    }

    @PostMapping("/answer")
    fun submitAnswer(
        @RequestParam userId: String,
        @RequestParam questionId: String,
        @RequestParam answer: Int
    ): Map<String, Any> {
        val isCorrect = gameService.processAnswer(userId, questionId, answer)
        return mapOf(
            "isCorrect" to isCorrect,
            "userId" to userId,
            "questionId" to questionId,
            "answer" to answer
        )
    }

    @GetMapping("/player/{userId}/answers")
    fun getPlayerAnswers(@PathVariable userId: String): Map<String, Any> {
        val answers = playerAnswerService.getPlayerAnswers(UUID.fromString(userId))
        return mapOf(
            "userId" to userId,
            "answers" to answers
        )
    }
} 