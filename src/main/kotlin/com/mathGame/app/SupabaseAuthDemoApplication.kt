package com.mathGame.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.mathGame.app.config.SupabaseProperties

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties::class)
class SupabaseAuthDemoApplication

fun main(args: Array<String>) {
    runApplication<SupabaseAuthDemoApplication>(*args)
} 