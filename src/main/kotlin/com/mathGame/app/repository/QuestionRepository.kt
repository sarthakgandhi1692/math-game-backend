package com.mathGame.app.repository

import com.mathGame.app.model.database.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuestionRepository : JpaRepository<Question, UUID> {
    @Query("SELECT q FROM Question q ORDER BY FUNCTION('RANDOM')")
    fun findRandomQuestions(limit: Int): List<Question>
} 