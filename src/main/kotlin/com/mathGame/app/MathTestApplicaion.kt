package com.mathGame.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.mathGame.app.config.SupabaseProperties

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties::class)
class MathTestApplication

fun main(args: Array<String>) {
    runApplication<MathTestApplication>(*args)
} 