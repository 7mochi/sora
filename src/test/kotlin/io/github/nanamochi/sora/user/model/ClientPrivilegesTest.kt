package io.github.nanamochi.sora.user.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientPrivilegesTest {
    @Test
    fun usesClientProtocolBits() {
        assertEquals(1, ClientPrivileges.PLAYER.bit)
        assertEquals(2, ClientPrivileges.MODERATOR.bit)
        assertEquals(4, ClientPrivileges.SUPPORTER.bit)
        assertEquals(8, ClientPrivileges.OWNER.bit)
        assertEquals(16, ClientPrivileges.DEVELOPER.bit)
        assertEquals(32, ClientPrivileges.TOURNAMENT.bit)
    }
}
