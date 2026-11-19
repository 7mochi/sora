package io.github.nanamochi.sora.shared.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun md5HexMatchesKnownHash() {
        assertEquals("5ebe2294ecd0e0f08eab7690d2a6ee69", hasher.md5Hex("secret"))
    }

    @Test
    fun verifiesPlainAndMd5() {
        val hash = hasher.hash("secret")

        assertTrue(hasher.matchesPlain("secret", hash))
        assertTrue(hasher.matchesMd5(hasher.md5Hex("secret"), hash))
        assertTrue(hasher.matchesMd5(hasher.md5Hex("secret").uppercase(), hash))
        assertFalse(hasher.matchesPlain("wrong", hash))
    }
}
