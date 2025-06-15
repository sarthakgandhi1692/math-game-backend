package com.mathGame.app.controller

import com.mathGame.app.service.DatabaseService
import com.zaxxer.hikari.HikariDataSource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class DatabaseTestController(
    private val databaseService: DatabaseService,
    private val dataSource: HikariDataSource
) {
    @GetMapping("/db")
    fun testDatabaseConnection(): Map<String, Any> {
        return try {
            // Get database version
            val result = databaseService.executeQuery("SELECT version()")
            val version = result.firstOrNull()?.get("version") as? String ?: "unknown"
            
            // Get connection pool stats
            val poolStats = mapOf(
                "maximumPoolSize" to dataSource.maximumPoolSize,
                "minimumIdle" to dataSource.minimumIdle,
                "idleTimeout" to dataSource.idleTimeout,
                "connectionTimeout" to dataSource.connectionTimeout,
                "maxLifetime" to dataSource.maxLifetime,
                "activeConnections" to dataSource.hikariPoolMXBean.activeConnections,
                "idleConnections" to dataSource.hikariPoolMXBean.idleConnections,
                "totalConnections" to dataSource.hikariPoolMXBean.totalConnections
            )

            mapOf(
                "status" to "success",
                "message" to "Database connection successful",
                "version" to version,
                "poolConfiguration" to poolStats
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "Database connection failed: ${e.message}",
                "error" to e.toString()
            )
        }
    }
} 