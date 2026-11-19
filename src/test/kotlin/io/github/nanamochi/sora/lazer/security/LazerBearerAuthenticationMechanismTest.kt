package io.github.nanamochi.sora.lazer.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LazerBearerAuthenticationMechanismTest {
    @Test
    fun hasPriorityAboveDefaultMechanisms() {
        val mechanism = LazerBearerAuthenticationMechanism()

        assertEquals(2000, mechanism.priority)
    }
}
