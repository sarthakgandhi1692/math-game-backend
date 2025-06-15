package com.mathGame.app.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.mathGame.app.model.game.Player
import com.mathGame.app.model.websocket.*
import com.mathGame.app.service.GameManager
import com.mathGame.app.service.GameEventListener
import com.mathGame.app.service.GameEventService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import com.mathGame.app.model.game.GameStatus
import jakarta.annotation.PostConstruct

@Component
class GameWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val gameManager: GameManager,
    private val gameEventService: GameEventService
) : TextWebSocketHandler(), GameEventListener {

    private val logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java)
    private val sessions = mutableMapOf<String, WebSocketSession>()
    private val userSessions = mutableMapOf<String, WebSocketSession>()
    private val sessionToUser = mutableMapOf<String, String>()
    private val userQuestionIndex = ConcurrentHashMap<String, Int>()

    @PostConstruct
    fun init() {
        gameEventService.addEventListener(this)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as String
        val email = session.attributes["email"] as String
        logger.info("WebSocket connection established for user: $userId ($email)")
        
        // Check if user already has an active session
        val existingSession = userSessions[userId]
        if (existingSession != null && existingSession.isOpen) {
            logger.warn("User $userId already has an active session. Closing old session.")
            existingSession.close(CloseStatus.POLICY_VIOLATION)
        }
        
        // Store session with both session ID and user ID as keys
        sessions[session.id] = session
        userSessions[userId] = session
        sessionToUser[session.id] = userId
        
        // Send welcome message
        val welcomeMessage = ConnectedMessage(
            userId = userId,
            message = "Successfully connected to game server"
        )
        
        sendToUser(userId, welcomeMessage)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val userId = sessionToUser[session.id] ?: return
        logger.info("Received message from user $userId: ${message.payload}")
        
        try {
            val gameMessage = objectMapper.readValue(message.payload, GameMessage::class.java)
            handleGameMessage(userId, gameMessage)
        } catch (e: Exception) {
            logger.error("Error processing message", e)
            val errorMessage = ErrorMessage(message = "Invalid message format: ${e.message}")
            sendToUser(userId, errorMessage)
        }
    }

    private fun handleGameMessage(userId: String, message: GameMessage) {
        when (message) {
            is JoinWaitingRoomMessage -> handleJoinWaitingRoom(userId)
            is AnswerSubmissionMessage -> handleAnswerSubmission(userId, message)
            is PingMessage -> handlePing(userId, message)
            else -> {
                logger.warn("Unhandled message type: ${message.type}")
                val errorMessage = ErrorMessage(message = "Unsupported message type: ${message.type}")
                sendToUser(userId, errorMessage)
            }
        }
    }

    private fun handleJoinWaitingRoom(userId: String) {
        val session = userSessions[userId] ?: return
        val email = session.attributes["email"] as String
        val name = session.attributes["name"] as? String ?: email.substringBefore("@")
        
        val player = Player(userId = userId, email = email, name = name)
        logger.info("Player $userId attempting to join waiting room")
        
        // Add player to waiting room
        val room = gameManager.addPlayerToWaitingRoom(player)
        
        if (room != null && room.player2 != null) {
            logger.info("Two players matched. Starting game in room: ${room.id}")
            // A game room was created with two players
            // Start the game and notify both players
            gameManager.startGame(room.id)
            
            // Reset question index for both players
            userQuestionIndex[room.player1.userId] = 0
            userQuestionIndex[room.player2!!.userId] = 0
            
            // Get player2 safely
            val player2 = room.player2!!
            
            // Send game started message to player 1
            val gameStartedMessage1 = GameStartedMessage(
                roomId = room.id,
                opponentId = player2.userId,
                opponentName = player2.name,
                startTime = Instant.now().toEpochMilli()
            )
            sendToUser(room.player1.userId, gameStartedMessage1)
            
            // Send game started message to player 2
            val gameStartedMessage2 = GameStartedMessage(
                roomId = room.id,
                opponentId = room.player1.userId,
                opponentName = room.player1.name,
                startTime = Instant.now().toEpochMilli()
            )
            sendToUser(player2.userId, gameStartedMessage2)
            
            // Send first question to both players
            sendNextQuestion(room.player1.userId)
            sendNextQuestion(player2.userId)
        } else {
            logger.info("Player $userId added to waiting room. Waiting for opponent...")
            // Player was added to waiting room
            sendToUser(userId, ErrorMessage(message = "Waiting for an opponent to join..."))
        }
    }

    private fun handleAnswerSubmission(userId: String, message: AnswerSubmissionMessage) {
        // Process the answer
        val isCorrect = gameManager.processAnswer(userId, message.questionId, message.answer)
        
        // Get the player's room
        val room = gameManager.getRoomForPlayer(userId)
        if (room == null) {
            sendToUser(userId, ErrorMessage(message = "You are not in an active game"))
            return
        }
        
        // Get opponent
        val opponent = room.getOpponent(userId)
        
        // Send score update
        val scoreUpdate = ScoreUpdateMessage(
            yourScore = if (userId == room.player1.userId) room.player1.score else room.player2?.score ?: 0,
            opponentScore = opponent?.score ?: 0
        )
        sendToUser(userId, scoreUpdate)
        
        // Send next question
        sendNextQuestion(userId)
    }

    private fun sendNextQuestion(userId: String) {
        val room = gameManager.getRoomForPlayer(userId) ?: return
        
        // Get current index for this user
        val currentIndex = userQuestionIndex.computeIfAbsent(userId) { 0 }
        
        // Check if we've reached the end of questions
        if (currentIndex >= room.questions.size) {
            // No more questions, wait for game to end naturally
            return
        }
        
        // Get the current question
        val question = room.questions[currentIndex]
        
        // Send question to user
        val questionMessage = QuestionMessage(
            questionId = question.id,
            expression = question.expression,
            questionNumber = currentIndex + 1,
            totalQuestions = room.questions.size
        )
        sendToUser(userId, questionMessage)
        
        // Increment index after sending the question
        userQuestionIndex[userId] = currentIndex + 1
    }

    private fun handlePing(userId: String, message: PingMessage) {
        // Simply echo back with server timestamp
        val response = PingMessage(timestamp = Instant.now().toEpochMilli())
        sendToUser(userId, response)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = sessionToUser[session.id]
        if (userId != null) {
            logger.info("WebSocket connection closed for user: $userId")
            
            // Remove user from waiting room
            gameManager.removePlayerFromWaitingRoom(userId)
            
            // Get the user's current game room if any
            val room = gameManager.getRoomForPlayer(userId)
            if (room != null) {
                // If game is still active, end it
                if (room.status == GameStatus.ACTIVE) {
                    gameManager.endGame(room.id)
                    
                    // Notify opponent about the disconnection
                    val opponent = room.getOpponent(userId)
                    if (opponent != null) {
                        sendToUser(opponent.userId, ErrorMessage(message = "Your opponent has disconnected. Game ended."))
                    }
                }
            }
            
            // Clean up session mappings
            userSessions.remove(userId)
            sessionToUser.remove(session.id)
        }
        sessions.remove(session.id)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error occurred", exception)
        session.close(CloseStatus.SERVER_ERROR)
    }

    fun sendToUser(userId: String, message: GameMessage) {
        try {
            val session = userSessions[userId]
            
            if (session?.isOpen == true) {
                val messageText = objectMapper.writeValueAsString(message)
                session.sendMessage(TextMessage(messageText))
            } else {
                logger.warn("No active session found for user: $userId")
            }
        } catch (e: Exception) {
            logger.error("Error sending message to user: $userId", e)
        }
    }

    fun broadcastToRoom(roomId: String, message: GameMessage, players: List<String>) {
        players.forEach { userId ->
            sendToUser(userId, message)
        }
    }

    override fun onGameEvent(event: GameMessage, recipients: List<String>) {
        recipients.forEach { userId ->
            sendToUser(userId, event)
        }
    }
} 