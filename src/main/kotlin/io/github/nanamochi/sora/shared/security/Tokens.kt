package io.github.nanamochi.sora.shared.security

import java.security.SecureRandom

object Tokens {
    private val random = SecureRandom()

    fun randomToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        return bytes.joinToString("") { "%02x".format(it) }
    }
}
