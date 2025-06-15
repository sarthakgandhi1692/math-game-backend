package com.mathGame.app.service

import com.mathGame.app.model.database.GameSession
import com.mathGame.app.model.game.GameRoom
import com.mathGame.app.model.game.GameStatus
import com.mathGame.app.model.game.Player
import com.mathGame.app.model.game.Question
import com.mathGame.app.model.websocket.GameEndedMessage
import com.mathGame.app.model.game.GameResult
import com.mathGame.app.model.websocket.GameResult as WebSocketGameResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class GameManager(
    private val questionService: QuestionService,
    private val gameSessionService: GameSessionService,
    private val playerAnswerService: PlayerAnswerService,
    private val leaderboardService: LeaderboardService,
    private val gameEventService: GameEventService
) {
    private val logger = LoggerFactory.getLogger(GameManager::class.java)
    private val waitingPlayers = mutableListOf<Player>()
    private val gameRooms = ConcurrentHashMap<String, GameRoom>()
    private val playerToRoomMap = ConcurrentHashMap<String, String>()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    
    private val GAME_DURATION_SECONDS = 60L
    private val QUESTIONS_PER_GAME = 20

    @Synchronized
    fun addPlayerToWaitingRoom(player: Player): GameRoom? {
        // Check if player is already in a game
        val existingRoomId = playerToRoomMap[player.userId]
        if (existingRoomId != null) {
            val room = gameRooms[existingRoomId]
            if (room != null && room.status != GameStatus.COMPLETED) {
                logger.info("Player ${player.userId} is already in game room ${room.id}")
                return room
            } else {
                // Clean up old room reference
                playerToRoomMap.remove(player.userId)
            }
        }

        // Check if player is already in waiting room
        if (waitingPlayers.any { it.userId == player.userId }) {
            logger.info("Player ${player.userId} is already in waiting room")
            return null
        }

        // Check if there's another player waiting
        if (waitingPlayers.isNotEmpty()) {
            val opponent = waitingPlayers.removeAt(0)
            
            // Create a new game room with both players
            val room = createGameRoom(opponent, player)
            logger.info("Created new game room ${room.id} with players ${opponent.userId} and ${player.userId}")
            
            return room
        } else {
            // Add player to waiting list
            waitingPlayers.add(player)
            logger.info("Added player ${player.userId} to waiting room")
            return null
        }
    }

    private fun createGameRoom(player1: Player, player2: Player): GameRoom {
        val room = GameRoom(player1 = player1, player2 = player2)
        
        // Generate questions for the game
        val questions = questionService.generateQuestions(QUESTIONS_PER_GAME)
        questions.forEach { room.addQuestion(it) }
        
        // Create game session in database
        val gameSession = gameSessionService.createGameSession(
            roomId = UUID.fromString(room.id),
            player1Id = UUID.fromString(player1.userId),
            player2Id = UUID.fromString(player2.userId)
        )
        
        // Map players to this room
        playerToRoomMap[player1.userId] = room.id
        playerToRoomMap[player2.userId] = room.id
        
        // Store the room
        gameRooms[room.id] = room
        
        // Schedule game end
        scheduleGameEnd(room)
        
        return room
    }

    fun startGame(roomId: String) {
        logger.info("Attempting to start game for room: $roomId")
        
        val room = gameRooms[roomId]
        if (room == null) {
            logger.error("No room found with id: $roomId")
            return
        }
        logger.info("Found room with status: ${room.status}")
        
        room.start()
        logger.info("Room status updated to: ${room.status}")

        // Fetch the game session by roomId
        val gameSession = gameSessionService.getSessionByRoomId(UUID.fromString(roomId))
        if (gameSession != null) {
            logger.info("Found game session with id: ${gameSession.id} and status: ${gameSession.status}")
            // Update game session status in database
            val updatedSession = gameSessionService.startGameSession(gameSession.id)
            logger.info("Game session status updated to: ${updatedSession.status}")
            logger.info("Game started in room ${room.id}")
        } else {
            logger.error("No game session found for room $roomId")
        }
    }

    private fun scheduleGameEnd(room: GameRoom) {
        scheduler.schedule({
            try {
                endGame(room.id)
            } catch (e: Exception) {
                logger.error("Error ending game for room ${room.id}", e)
            }
        }, GAME_DURATION_SECONDS, TimeUnit.SECONDS)
    }

    private fun convertGameResult(result: GameResult): WebSocketGameResult {
        return when (result) {
            GameResult.WIN -> WebSocketGameResult.WIN
            GameResult.LOSE -> WebSocketGameResult.LOSE
            GameResult.DRAW -> WebSocketGameResult.DRAW
        }
    }

    fun endGame(roomId: String) {
        val room = gameRooms[roomId] ?: return
        room.end()
        
        // Get the game session by room ID
        val gameSession = gameSessionService.getSessionByRoomId(UUID.fromString(roomId))
        if (gameSession != null) {
            // Update game session status in database
            gameSessionService.endGameSession(gameSession.id)
            
            // Update leaderboard scores and send end game messages
            room.player1.let { player1 ->
                leaderboardService.updatePlayerScore(
                    playerId = UUID.fromString(player1.userId),
                    email = player1.email,
                    score = player1.score
                )
                
                // Send end game message to player 1
                val gameEndedMessage1 = GameEndedMessage(
                    yourScore = player1.score,
                    opponentScore = room.player2?.score ?: 0,
                    result = convertGameResult(room.getResult(player1.userId)),
                    correctAnswers = player1.correctAnswers,
                    totalQuestions = room.questions.size
                )
                gameEventService.emit(gameEndedMessage1, listOf(player1.userId))
            }
            
            room.player2?.let { player2 ->
                leaderboardService.updatePlayerScore(
                    playerId = UUID.fromString(player2.userId),
                    email = player2.email,
                    score = player2.score
                )
                
                // Send end game message to player 2
                val gameEndedMessage2 = GameEndedMessage(
                    yourScore = player2.score,
                    opponentScore = room.player1.score,
                    result = convertGameResult(room.getResult(player2.userId)),
                    correctAnswers = player2.correctAnswers,
                    totalQuestions = room.questions.size
                )
                gameEventService.emit(gameEndedMessage2, listOf(player2.userId))
            }
            
            logger.info("Game ended in room ${room.id}")
            
            // Clean up player mappings
            playerToRoomMap.remove(room.player1.userId)
            room.player2?.let { player2 ->
                playerToRoomMap.remove(player2.userId)
            }
            
            // Remove the room
            gameRooms.remove(roomId)
        } else {
            logger.error("No game session found for room $roomId")
        }
    }

    fun processAnswer(userId: String, questionId: String, answer: Int): Boolean {
        val roomId = playerToRoomMap[userId] ?: return false
        val room = gameRooms[roomId] ?: return false
        
        if (room.status != GameStatus.ACTIVE) {
            return false
        }
        
        // Get the question to access its expression
        val question = room.questions.find { it.id == questionId }
        if (question == null) {
            logger.error("Question $questionId not found in room $roomId")
            return false
        }
        
        room.recordAnswer(userId, questionId, answer)
        val isCorrect = room.isAnswerCorrect(userId, questionId)
        
        if (isCorrect) {
            room.updateScore(userId, true)
        }
        
        // Save answer to database with expression
        playerAnswerService.saveAnswer(
            gameSessionId = UUID.fromString(roomId),
            playerId = UUID.fromString(userId),
            questionId = UUID.fromString(questionId),
            answer = answer,
            isCorrect = isCorrect,
            expression = question.expression
        )
        
        return isCorrect
    }

    fun getRoomForPlayer(userId: String): GameRoom? {
        val roomId = playerToRoomMap[userId] ?: return null
        return gameRooms[roomId]
    }

    fun getQuestion(roomId: String, index: Int): Question? {
        val room = gameRooms[roomId] ?: return null
        return if (index < room.questions.size) room.questions[index] else null
    }

    @Synchronized
    fun removePlayerFromWaitingRoom(userId: String) {
        waitingPlayers.removeIf { it.userId == userId }
        logger.info("Removed player $userId from waiting room")
    }
} 