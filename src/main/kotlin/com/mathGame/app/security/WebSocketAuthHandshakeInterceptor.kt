package com.mathGame.app.security

import com.auth0.jwt.exceptions.JWTVerificationException
import com.mathGame.app.service.SupabaseJwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder

@Component
class WebSocketAuthHandshakeInterceptor(
    private val supabaseJwtService: SupabaseJwtService
) : HandshakeInterceptor {
    private val logger = LoggerFactory.getLogger(WebSocketAuthHandshakeInterceptor::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        try {
            logger.debug("Processing WebSocket handshake request: ${request.uri}")
            
            // Extract token from URL parameters
            val uri = UriComponentsBuilder.fromUri(request.uri).build()
            val token = uri.queryParams.getFirst("token")
            
            if (token == null) {
                logger.error("No token provided in WebSocket connection request")
                response.setStatusCode(HttpStatus.UNAUTHORIZED)
                return false
            }

            try {
                logger.debug("Verifying JWT token...")
                val decodedJWT = supabaseJwtService.verifyToken(token)
                logger.debug("WebSocket JWT verified successfully")

                // Extract user info from claims
                val userId = decodedJWT.getClaim("sub").asString()
                val email = decodedJWT.getClaim("email").asString()
                val role = decodedJWT.getClaim("role").asString() ?: "authenticated"

                logger.debug("WebSocket user authenticated - ID: $userId, Email: $email, Role: $role")

                // Store user information in attributes for later use
                attributes["userId"] = userId
                attributes["email"] = email
                attributes["role"] = role
                attributes["name"] = email.substringBefore("@") // Default name from email

                val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug("WebSocket security context updated with authorities: $authorities")

                return true
            } catch (e: JWTVerificationException) {
                logger.error("WebSocket JWT verification failed: ${e.message}", e)
                response.setStatusCode(HttpStatus.UNAUTHORIZED)
                return false
            }
        } catch (e: Exception) {
            logger.error("Error during WebSocket handshake: ${e.message}", e)
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        if (exception != null) {
            logger.error("Error after WebSocket handshake: ${exception.message}", exception)
            SecurityContextHolder.clearContext()
        } else {
            logger.debug("WebSocket handshake completed successfully")
        }
    }
} 