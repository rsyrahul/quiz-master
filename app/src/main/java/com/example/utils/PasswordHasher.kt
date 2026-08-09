package com.example.utils

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String? = null): Pair<String, String> {
        val actualSalt = salt ?: generateSalt()
        val saltedPassword = actualSalt + password
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
        val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
        return Pair(hashHex, actualSalt)
    }

    fun verifyPassword(password: String, storedHash: String, salt: String): Boolean {
        val (computedHash, _) = hashPassword(password, salt)
        return computedHash.equals(storedHash, ignoreCase = true)
    }
}
