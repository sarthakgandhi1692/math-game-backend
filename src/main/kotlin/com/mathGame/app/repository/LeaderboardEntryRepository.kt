package com.mathGame.app.repository

import com.mathGame.app.model.database.LeaderboardEntry
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeaderboardEntryRepository : JpaRepository<LeaderboardEntry, UUID> {
    fun findByPlayerId(playerId: UUID): LeaderboardEntry?
    fun findAllByOrderByTotalScoreDesc(pageable: Pageable): List<LeaderboardEntry>
    fun findTop10ByOrderByTotalScoreDesc(): List<LeaderboardEntry>
} 