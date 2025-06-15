package com.mathGame.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import java.io.File
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.ConfigurableEnvironment
import java.util.Properties

@Configuration
class EnvConfig(private val environment: ConfigurableEnvironment) {
    private val logger = LoggerFactory.getLogger(EnvConfig::class.java)

    @PostConstruct
    fun init() {
        try {
            val envFile = File(".env")
            if (envFile.exists()) {
                logger.info("Loading environment variables from .env file")
                val props = Properties()
                
                envFile.readLines()
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .forEach { line ->
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()
                            props.setProperty(key, value)
                            
                            // Also set as system property if not already set
                            if (System.getProperty(key) == null) {
                                System.setProperty(key, value)
                            }
                            
                            // Special handling for Supabase URL to ensure it has http/https
                            if (key == "SUPABASE_URL" && !value.startsWith("http")) {
                                val correctedUrl = "https://$value"
                                props.setProperty(key, correctedUrl)
                                System.setProperty(key, correctedUrl)
                                logger.info("Corrected Supabase URL to include https: $correctedUrl")
                            }
                        }
                    }
                
                // Add to environment
                environment.propertySources.addFirst(PropertiesPropertySource("dotenv", props))
                logger.info("Environment variables loaded successfully")
                
                // Log the Supabase URL for verification
                logger.info("Supabase URL: ${props.getProperty("SUPABASE_URL")}")
            } else {
                logger.warn(".env file not found")
            }
        } catch (e: Exception) {
            logger.error("Error loading .env file", e)
        }
    }
} 