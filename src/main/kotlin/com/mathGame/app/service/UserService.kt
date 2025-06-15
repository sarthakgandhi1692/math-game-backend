package com.mathGame.app.service

import com.mathGame.app.model.database.User
import com.mathGame.app.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun createUser(email: String, name: String): User {
        return userRepository.save(User(email = email, name = name))
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: UUID): User? {
        return userRepository.findById(id).orElse(null)
    }

    @Transactional
    fun getOrCreateUser(email: String, name: String): User {
        return findByEmail(email) ?: createUser(email, name)
    }
} 