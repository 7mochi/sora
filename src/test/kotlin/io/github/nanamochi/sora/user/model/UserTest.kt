package io.github.nanamochi.sora.user.model

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun mapsSupporterAndModeratorToClientBits() {
        val user =
            User().apply {
                group = GroupIdentifier.GMT
                osuSubscriber = true
            }

        assertEquals(1 or 2 or 4, user.clientPrivileges)
    }

    @Test
    fun mapsAdminAndDeveloperToClientBits() {
        val admin = User().apply { group = GroupIdentifier.ADMIN }
        val developer = User().apply { group = GroupIdentifier.DEV }

        assertEquals(1 or 8, admin.clientPrivileges)
        assertEquals(1 or 16, developer.clientPrivileges)
    }

    @Test
    fun restrictionComesFromRestrictionTime() {
        val user = User()
        user.restrictionTime = Instant.now()

        assertEquals(true, user.isRestricted)
    }

    @Test
    fun silenceLastsUntilSilenceEnd() {
        val user = User()
        user.silenceEnd = Instant.now().plusSeconds(60)

        assertTrue(user.isSilenced)

        user.silenceEnd = Instant.now().minusSeconds(1)
        assertFalse(user.isSilenced)
    }
}
