package com.mathGame.app.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mathGame.app.config.SupabaseProperties
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TokenController(
    private val supabaseProperties: SupabaseProperties
) {
    data class TokenRequest(
        val userId: String,
        val email: String,
        val role: String = "authenticated"
    )

    data class TokenResponse(
        val token: String,
        val expiresIn: Long = 3600 // 1 hour in seconds
    )

    private val jwtSecret: String = extractJwtSecret(supabaseProperties.anonKey)

    @PostMapping("/api/public/generate-token")
    fun generateToken(@RequestBody request: TokenRequest): TokenResponse {
        val now = Date()
        val expiresAt = Date(now.time + 12 * 3600 * 1000) // 1 hour from now

        val token = JWT.create()
            .withIssuer("supabase")
            .withSubject(request.userId)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .withClaim("email", request.email)
            .withClaim("role", request.role)
            .withClaim("aud", "authenticated")
            .sign(Algorithm.HMAC256(jwtSecret))

        return TokenResponse(
            token = token,
            expiresIn = 3600
        )
    }

    private fun extractJwtSecret(anonKey: String): String {
        val parts = anonKey.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid anon key format")
        }

        val decoder = Base64.getUrlDecoder()
        val payload = String(decoder.decode(parts[1]))
        return payload.substringAfter("\"ref\":\"").substringBefore("\"")
    }
} 