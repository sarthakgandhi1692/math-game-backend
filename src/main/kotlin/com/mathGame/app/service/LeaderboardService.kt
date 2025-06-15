package com.mathGame.app.service

import com.mathGame.app.model.database.LeaderboardEntry
import com.mathGame.app.repository.LeaderboardEntryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeaderboardService(
    private val leaderboardEntryRepository: LeaderboardEntryRepository
) {
    @Transactional
    fun updatePlayerScore(playerId: UUID, email: String, score: Int) {
        val existingEntry = leaderboardEntryRepository.findByPlayerId(playerId)
        if (existingEntry != null) {
            leaderboardEntryRepository.save(existingEntry.copy(
                totalScore = existingEntry.totalScore + score,
                totalGames = existingEntry.totalGames + 1
            ))
        } else {
            leaderboardEntryRepository.save(
                LeaderboardEntry(
                    playerId = playerId,
                    email = email,
                    totalScore = score,
                    totalGames = 1
                )
            )
        }
    }

    fun getTopPlayers(limit: Int = 10): List<LeaderboardEntry> {
        return leaderboardEntryRepository.findTop10ByOrderByTotalScoreDesc()
    }

    fun getPlayerRanking(playerId: UUID): LeaderboardEntry? {
        return leaderboardEntryRepository.findByPlayerId(playerId)
    }

    fun getLeaderboardPage(page: Int, size: Int): List<LeaderboardEntry> {
        return leaderboardEntryRepository.findAllByOrderByTotalScoreDesc(PageRequest.of(page, size))
    }

    @Transactional
    fun resetLeaderboard() {
        // This is a dangerous operation and should be used with caution
        leaderboardEntryRepository.deleteAll()
    }
} 