package com.example.pupilmesh.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private const val SALT_LENGTH = 32
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt.toHexString()
    }

    fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.toHexString()
    }

    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash == storedHash
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
} 