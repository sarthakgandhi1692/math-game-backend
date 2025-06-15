package com.mathGame.app.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.mathGame.app.config.SupabaseProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class SupabaseJwtService(
    private val supabaseProperties: SupabaseProperties
) {
    private val logger = LoggerFactory.getLogger(SupabaseJwtService::class.java)
    private val jwtSecret: String = extractJwtSecret(supabaseProperties.anonKey)

    fun verifyToken(token: String): DecodedJWT {
        try {
            // First decode without verification to check claims
            val unverified = JWT.decode(token)
            logger.debug("Token claims before verification:")
            logger.debug("Issuer: ${unverified.issuer}")
            logger.debug("Subject: ${unverified.subject}")
            logger.debug("Role: ${unverified.getClaim("role").asString()}")
            logger.debug("Email: ${unverified.getClaim("email").asString()}")

            // Use HS256 with the extracted JWT secret
            val algorithm = Algorithm.HMAC256(jwtSecret)
            val verifier = JWT.require(algorithm)
                .withIssuer("supabase")
                .build()

            logger.debug("Attempting to verify token with HS256")
            val verified = verifier.verify(token)
            logger.debug("Token verified successfully")
            return verified
        } catch (e: Exception) {
            logger.error("Token verification failed", e)
            throw e
        }
    }

    private fun extractJwtSecret(anonKey: String): String {
        // The anon key is a JWT token itself
        // We need to extract the JWT secret from its payload
        val parts = anonKey.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid anon key format")
        }

        // Decode the payload (second part)
        val decoder = Base64.getUrlDecoder()
        val payload = String(decoder.decode(parts[1]))
        logger.debug("Extracted JWT secret from anon key")
        
        // The JWT secret is the reference ID (ref) in the payload
        // This is what Supabase uses to sign tokens
        return payload.substringAfter("\"ref\":\"").substringBefore("\"")
    }
} 