package com.mathGame.app.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class TestController {
    private val logger = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/public/test")
    fun publicEndpoint(): Map<String, String> {
        logger.debug("Public endpoint accessed")
        return mapOf("message" to "This is a public endpoint")
    }

    @GetMapping("/protected")
    fun protectedEndpoint(@AuthenticationPrincipal userId: String): Map<String, Any> {
        logger.debug("Protected endpoint accessed by user: $userId")
        return mapOf(
            "message" to "This is a protected endpoint",
            "userId" to userId
        )
    }

    // Additional test endpoint with more details
    @GetMapping("/protected/details")
    fun protectedDetailsEndpoint(@AuthenticationPrincipal userId: String): Map<String, Any> {
        logger.debug("Protected details endpoint accessed by user: $userId")
        return mapOf(
            "message" to "This is a protected endpoint with details",
            "userId" to userId,
            "timestamp" to System.currentTimeMillis(),
            "status" to "authenticated"
        )
    }
} 