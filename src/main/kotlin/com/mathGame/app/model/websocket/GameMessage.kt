package com.mathGame.app.model.websocket

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JoinWaitingRoomMessage::class, name = "JOIN_WAITING_ROOM"),
    JsonSubTypes.Type(value = AnswerSubmissionMessage::class, name = "ANSWER_SUBMISSION"),
    JsonSubTypes.Type(value = PingMessage::class, name = "PING"),
    JsonSubTypes.Type(value = GameStartedMessage::class, name = "GAME_STARTED"),
    JsonSubTypes.Type(value = QuestionMessage::class, name = "QUESTION"),
    JsonSubTypes.Type(value = ScoreUpdateMessage::class, name = "SCORE_UPDATE"),
    JsonSubTypes.Type(value = GameEndedMessage::class, name = "GAME_ENDED"),
    JsonSubTypes.Type(value = ConnectedMessage::class, name = "CONNECTED"),
    JsonSubTypes.Type(value = ErrorMessage::class, name = "ERROR")
)
interface GameMessage {
    val type: MessageType
}

enum class MessageType {
    JOIN_WAITING_ROOM,
    ANSWER_SUBMISSION,
    PING,
    GAME_STARTED,
    QUESTION,
    SCORE_UPDATE,
    GAME_ENDED,
    CONNECTED,
    ERROR
}

data class JoinWaitingRoomMessage(
    override val type: MessageType = MessageType.JOIN_WAITING_ROOM
) : GameMessage

data class AnswerSubmissionMessage(
    override val type: MessageType = MessageType.ANSWER_SUBMISSION,
    val questionId: String,
    val answer: Int,
    val timestamp: Long
) : GameMessage

data class PingMessage(
    override val type: MessageType = MessageType.PING,
    val timestamp: Long
) : GameMessage

data class GameStartedMessage(
    override val type: MessageType = MessageType.GAME_STARTED,
    val roomId: String,
    val opponentId: String,
    val opponentName: String,
    val startTime: Long
) : GameMessage

data class QuestionMessage(
    override val type: MessageType = MessageType.QUESTION,
    val questionId: String,
    val expression: String,
    val questionNumber: Int,
    val totalQuestions: Int
) : GameMessage

data class ScoreUpdateMessage(
    override val type: MessageType = MessageType.SCORE_UPDATE,
    val yourScore: Int,
    val opponentScore: Int
) : GameMessage

data class GameEndedMessage(
    override val type: MessageType = MessageType.GAME_ENDED,
    val yourScore: Int,
    val opponentScore: Int,
    val result: GameResult,
    val correctAnswers: Int,
    val totalQuestions: Int
) : GameMessage

data class ConnectedMessage(
    override val type: MessageType = MessageType.CONNECTED,
    val userId: String,
    val message: String
) : GameMessage

data class ErrorMessage(
    override val type: MessageType = MessageType.ERROR,
    val message: String
) : GameMessage

enum class GameResult {
    WIN,
    LOSE,
    DRAW
} 