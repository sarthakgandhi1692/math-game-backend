package com.mathGame.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "supabase")
data class SupabaseProperties(
    val apiUrl: String,
    val anonKey: String,
    val jwt: JwtConfig,
    val database: DatabaseConfig
) {
    data class JwtConfig(
        val secret: String
    )

    data class DatabaseConfig(
        var url: String = "",
        var username: String = "",
        var password: String = "",
        var schema: String = "public"
    ) 
}
