package com.mathGame.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.*
import org.springframework.web.socket.config.annotation.*
import com.mathGame.app.websocket.GameWebSocketHandler
import com.mathGame.app.security.WebSocketAuthHandshakeInterceptor
import org.springframework.web.socket.server.support.DefaultHandshakeHandler

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val gameWebSocketHandler: GameWebSocketHandler,
    private val webSocketAuthHandshakeInterceptor: WebSocketAuthHandshakeInterceptor
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(gameWebSocketHandler, "/ws")
            .addInterceptors(webSocketAuthHandshakeInterceptor)
            .setAllowedOriginPatterns("*")

    }
}
