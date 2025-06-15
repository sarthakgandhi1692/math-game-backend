package com.mathGame.app.model.database

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String
) {
    // Default constructor required by JPA
    constructor() : this(
        id = UUID.randomUUID(),
        email = "",
        name = ""
    )
} 