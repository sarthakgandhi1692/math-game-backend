package com.mathGame.app.controller

import com.auth0.jwt.exceptions.JWTVerificationException
import com.mathGame.app.service.SupabaseJwtService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val supabaseJwtService: SupabaseJwtService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    data class TokenVerificationResponse(
        val userId: String,
        val email: String?,
        val role: String,
        val isValid: Boolean,
        val expiresAt: Long?
    )

    @PostMapping("/verify")
    fun verifyToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<TokenVerificationResponse> {
        return try {
            // Extract token from Authorization header
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    TokenVerificationResponse(
                        userId = "",
                        email = null,
                        role = "",
                        isValid = false,
                        expiresAt = null
                    )
                )
            }

            val token = authHeader.substring(7) // Remove "Bearer " prefix
            logger.debug("Verifying token")

            // Verify token using SupabaseJwtService
            val decodedJWT = supabaseJwtService.verifyToken(token)

            // Extract claims
            val userId = decodedJWT.subject
            val email = decodedJWT.getClaim("email").asString()
            val role = decodedJWT.getClaim("role").asString() ?: "authenticated"
            val expiresAt = decodedJWT.expiresAt.time

            logger.debug("Token verified successfully for user: $userId with role: $role")

            ResponseEntity.ok(
                TokenVerificationResponse(
                    userId = userId,
                    email = email,
                    role = role,
                    isValid = true,
                    expiresAt = expiresAt
                )
            )

        } catch (e: JWTVerificationException) {
            logger.error("Token verification failed", e)
            ResponseEntity.badRequest().body(
                TokenVerificationResponse(
                    userId = "",
                    email = null,
                    role = "",
                    isValid = false,
                    expiresAt = null
                )
            )
        } catch (e: Exception) {
            logger.error("Unexpected error during token verification", e)
            ResponseEntity.internalServerError().body(
                TokenVerificationResponse(
                    userId = "",
                    email = null,
                    role = "",
                    isValid = false,
                    expiresAt = null
                )
            )
        }
    }
} 