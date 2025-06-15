package com.mathGame.app.controller

import com.mathGame.app.service.DatabaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class DatabaseTestController(private val databaseService: DatabaseService) {

    @GetMapping("/db")
    fun testDatabaseConnection(): Map<String, Any> {
        return try {
            // Try to execute a simple query
            val result = databaseService.executeQuery("SELECT version()")
            mapOf(
                "status" to "success",
                "message" to "Database connection successful",
                "version" to (result.firstOrNull()?.get("version") as? String ?: "unknown")
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "Database connection failed: ${e.message}"
            )
        }
    }
} 