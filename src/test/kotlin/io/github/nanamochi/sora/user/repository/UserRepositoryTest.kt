package io.github.nanamochi.sora.user.repository

import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.model.User
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class UserRepositoryTest {
    @Inject lateinit var users: UserRepository

    @Test
    @TestTransaction
    fun savesAndFindsUser() {
        val user =
            User().apply {
                username = "player"
                safeUsername = "player"
                email = "player@example.com"
                passwordBcrypt = "hash"
                country = CountryCode.PE
                registrationTime = Instant.now()
                latestActivity = Instant.now()
            }

        users.persist(user)

        assertEquals("player", users.findBySafeUsername("player")?.username)
        assertEquals(user.id, users.findByEmail("player@example.com")?.id)
    }
}
