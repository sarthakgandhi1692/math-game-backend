package com.mathGame.app.service

import com.mathGame.app.model.game.GameRoom
import com.mathGame.app.model.websocket.GameMessage
import org.springframework.stereotype.Service

interface GameEventListener {
    fun onGameEvent(event: GameMessage, recipients: List<String>)
}

@Service
class GameEventService {
    private val listeners = mutableListOf<GameEventListener>()

    fun addEventListener(listener: GameEventListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: GameEventListener) {
        listeners.remove(listener)
    }

    fun emit(event: GameMessage, recipients: List<String>) {
        listeners.forEach { listener ->
            listener.onGameEvent(event, recipients)
        }
    }
} 