package com.mathGame.app.controller

import com.mathGame.app.service.LeaderboardService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {
    @GetMapping
    fun getLeaderboard(): Map<String, Any> {
        val topPlayers = leaderboardService.getTopPlayers()
        return mapOf(
            "topPlayers" to topPlayers
        )
    }
} 