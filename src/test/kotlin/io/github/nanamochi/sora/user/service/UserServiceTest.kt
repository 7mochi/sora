package io.github.nanamochi.sora.user.service

import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class UserServiceTest {
    @Inject lateinit var users: UserService
    @Inject lateinit var repository: UserRepository

    @Test
    @TestTransaction
    fun createsUserWithStats() {
        val user = users.create("Player One", "player@example.com", "secret123", CountryCode.PE)

        assertEquals("player_one", user.safeUsername)
        assertEquals(4, users.findStatistics(user.id).size)
    }

    @Test
    @TestTransaction
    fun rejectsDuplicateUsername() {
        users.create("duplicate", "duplicate@example.com", "secret123", CountryCode.PE)

        val errors = users.validateRegistration("duplicate", "other@example.com", "secret123")

        assertTrue(errors.username.isNotEmpty())
    }

    @Test
    @TestTransaction
    fun rejectsDuplicateEmail() {
        users.create("first_player", "same@example.com", "secret123", CountryCode.PE)

        val errors = users.validateRegistration("second_player", "same@example.com", "secret123")

        assertTrue(errors.email.isNotEmpty())
    }

    @Test
    fun rejectsDuplicateUserOnPersist() {
        val username = uniqueUsername("race")
        users.create(username, "$username@example.com", "secret123", CountryCode.PE)

        val error =
            assertThrows(ServiceException::class.java) {
                users.create(username, "other_$username@example.com", "secret123", CountryCode.PE)
            }

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, error.errorCode)
    }

    @Test
    fun rejectsInvalidUsername() {
        val errors = users.validateRegistration("x", "short@example.com", "secret123")

        assertTrue(errors.username.isNotEmpty())
    }

    @Test
    fun rejectsLongUsername() {
        val username = "a".repeat(32) + "!!!!"

        val errors = users.validateRegistration(username, "long@example.com", "secret123")

        assertTrue(errors.username.isNotEmpty())
    }

    @Test
    fun rejectsUsernameWithSpaceAndUnderscore() {
        val errors = users.validateRegistration("player _one", "space@example.com", "secret123")

        assertTrue(errors.username.isNotEmpty())
    }

    @Test
    fun rejectsWeakPassword() {
        val errors = users.validateRegistration("weak_player", "weak@example.com", "password")

        assertTrue(errors.password.isNotEmpty())
    }

    @Test
    fun rejectsPasswordContainingUsername() {
        val errors = users.validateRegistration("player", "contains@example.com", "player123")

        assertTrue(errors.password.isNotEmpty())
    }

    @Test
    fun rejectsInvalidEmail() {
        val errors = users.validateRegistration("email_player", "not-an-email", "secret123")

        assertTrue(errors.email.isNotEmpty())
    }

    @Test
    fun rejectsLongPassword() {
        val errors =
            users.validateRegistration(
                "longpw_player",
                "longpw@example.com",
                "abcdefghijklmnopqrstuvwxyz1234567",
            )

        assertTrue(errors.password.isNotEmpty())
    }

    @Test
    fun rejectsPasswordWithTooFewUniqueCharacters() {
        val errors = users.validateRegistration("unique_player", "unique@example.com", "aaabbbccc")

        assertTrue(errors.password.isNotEmpty())
    }

    @Test
    @TestTransaction
    fun findsByUsernameOrEmail() {
        val user = users.create("lookup_player", "lookup@example.com", "secret123", CountryCode.PE)

        assertEquals(user.id, users.findByUsernameOrEmail("@lookup_player")?.id)
        assertEquals(user.id, users.findByUsernameOrEmail("lookup@example.com")?.id)
    }

    @Test
    @TestTransaction
    fun updatesLatestActivity() {
        val user =
            users.create("activity_player", "activity@example.com", "secret123", CountryCode.PE)
        user.latestActivity = Instant.EPOCH

        users.updateLatestActivity(user.id)

        assertEquals(1, repository.count("id = ?1 and latestActivity > ?2", user.id, Instant.EPOCH))
    }
}
