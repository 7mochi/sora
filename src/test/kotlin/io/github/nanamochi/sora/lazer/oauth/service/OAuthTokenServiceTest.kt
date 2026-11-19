package io.github.nanamochi.sora.lazer.oauth.service

import io.github.nanamochi.sora.lazer.oauth.repository.OAuthAccessTokenRepository
import io.github.nanamochi.sora.lazer.oauth.repository.OAuthRefreshTokenRepository
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.utils.createUser
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class OAuthTokenServiceTest {
    @Inject lateinit var tokens: OAuthTokenService
    @Inject lateinit var accessTokens: OAuthAccessTokenRepository
    @Inject lateinit var refreshTokens: OAuthRefreshTokenRepository
    @Inject lateinit var users: UserRepository
    @Inject lateinit var hasher: PasswordHasher

    @Test
    @TestTransaction
    fun createsAndValidatesAccessToken() {
        val user = createUser(users, hasher, "oauth_token")
        val issued = tokens.create(user.id)

        assertEquals(user.id, tokens.validateAccessToken(issued.accessToken).userId)
    }

    @Test
    @TestTransaction
    fun rotatesRefreshAndInvalidatesBothOldTokens() {
        val user = createUser(users, hasher, "oauth_rotate")
        val issued = tokens.create(user.id)

        val rotated = tokens.rotateRefreshToken(issued.refreshToken)

        assertNotEquals(issued.accessToken, rotated.accessToken)
        assertNotEquals(issued.refreshToken, rotated.refreshToken)
        assertThrows(ServiceException::class.java) {
            tokens.validateAccessToken(issued.accessToken)
        }
        assertThrows(ServiceException::class.java) {
            tokens.rotateRefreshToken(issued.refreshToken)
        }
    }

    @Test
    @TestTransaction
    fun revokesAccessToken() {
        val user = createUser(users, hasher, "oauth_revoke")
        val issued = tokens.create(user.id)

        tokens.revokeAccessToken(issued.accessToken)

        assertThrows(ServiceException::class.java) {
            tokens.validateAccessToken(issued.accessToken)
        }
    }

    @Test
    @TestTransaction
    fun rejectsExpiredAccessToken() {
        val user = createUser(users, hasher, "oauth_exp_acc")
        val issued = tokens.create(user.id)
        val access = checkNotNull(accessTokens.findByToken(issued.accessToken))
        access.expiresAt = Instant.now().minusSeconds(1)

        val error =
            assertThrows(ServiceException::class.java) {
                tokens.validateAccessToken(issued.accessToken)
            }

        assertEquals(ErrorCode.AUTH_SESSION_EXPIRED, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsMissingAccessToken() {
        val error =
            assertThrows(ServiceException::class.java) { tokens.validateAccessToken("missing") }

        assertEquals(ErrorCode.AUTH_SESSION_NOT_FOUND, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsExpiredRefreshToken() {
        val user = createUser(users, hasher, "oauth_exp_ref")
        val issued = tokens.create(user.id)
        val refresh = checkNotNull(refreshTokens.findByToken(issued.refreshToken))
        refresh.expiresAt = Instant.now().minusSeconds(1)

        val error =
            assertThrows(ServiceException::class.java) {
                tokens.rotateRefreshToken(issued.refreshToken)
            }

        assertEquals(ErrorCode.AUTH_INVALID_GRANT, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsMissingRefreshToken() {
        val error =
            assertThrows(ServiceException::class.java) { tokens.rotateRefreshToken("missing") }

        assertEquals(ErrorCode.AUTH_INVALID_GRANT, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsRefreshWhenAccessTokenIsMissing() {
        val user = createUser(users, hasher, "oauth_missing")
        val issued = tokens.create(user.id)
        val access = checkNotNull(accessTokens.findByToken(issued.accessToken))
        accessTokens.deleteById(access.id)

        val error =
            assertThrows(ServiceException::class.java) {
                tokens.rotateRefreshToken(issued.refreshToken)
            }

        assertEquals(ErrorCode.AUTH_INVALID_GRANT, error.errorCode)
    }

    @Test
    @TestTransaction
    fun refreshExpiresInThreeCalendarMonths() {
        val user = createUser(users, hasher, "oauth_calendar")
        val issued = tokens.create(user.id)

        val refresh = checkNotNull(refreshTokens.findByToken(issued.refreshToken))
        val expected = ZonedDateTime.now(ZoneOffset.UTC).plusMonths(3).toInstant()
        val difference = Duration.between(expected, refresh.expiresAt).abs().seconds
        assertTrue(difference <= 5)
    }
}
