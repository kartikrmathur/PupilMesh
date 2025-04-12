package com.example.pupilmesh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val passwordHash: String, // We'll store hashed passwords, not plain text
    val salt: String // For password hashing
) 