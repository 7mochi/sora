package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.repository.BanchoSessionRepository
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.model.GroupIdentifier
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.utils.createUser
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class BanchoSessionServiceTest {
    @Inject lateinit var sessions: BanchoSessionService
    @Inject lateinit var users: UserRepository
    @Inject lateinit var repository: BanchoSessionRepository
    @Inject lateinit var hasher: PasswordHasher

    private val clientInfo = ClientInfo("127.0.0.1", "test")

    @Test
    @TestTransaction
    fun createsSessionWithPingTimeout() {
        val user = createUser(users, hasher, "bancho_store")

        val session = sessions.create(user, clientInfo)
        val remaining = Duration.between(Instant.now(), session.expiresAt).seconds

        assertEquals(user.id, session.userId)
        assertEquals(clientInfo.ipAddress, session.ipAddress)
        assertEquals(clientInfo.userAgent, session.userAgent)
        assertTrue(session.accessToken.isNotBlank())
        assertTrue(remaining in 295..300)
    }

    @Test
    @TestTransaction
    fun extendsActiveSession() {
        val user = createUser(users, hasher, "bancho_extend")
        val session = sessions.create(user, clientInfo)
        session.expiresAt = Instant.now().plusSeconds(1)

        val extended = sessions.extendSession(session.accessToken)
        val remaining = Duration.between(Instant.now(), extended.expiresAt).seconds

        assertTrue(remaining in 295..300)
    }

    @Test
    @TestTransaction
    fun rejectsExpiredSession() {
        val user = createUser(users, hasher, "bancho_expired")
        val session = sessions.create(user, clientInfo)
        session.expiresAt = Instant.now().minusSeconds(1)

        val error =
            assertThrows(ServiceException::class.java) {
                sessions.extendSession(session.accessToken)
            }

        assertEquals(ErrorCode.AUTH_SESSION_EXPIRED, error.errorCode)
    }

    @Test
    @TestTransaction
    fun revokedSessionCannotBeExtended() {
        val user = createUser(users, hasher, "bancho_revoke")
        val session = sessions.create(user, clientInfo)

        sessions.revoke(session.accessToken)

        assertNull(repository.findByToken(session.accessToken))
        val error =
            assertThrows(ServiceException::class.java) {
                sessions.extendSession(session.accessToken)
            }
        assertEquals(ErrorCode.AUTH_SESSION_NOT_FOUND, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsSessionWhenActiveLimitReached() {
        val user = createUser(users, hasher, "bancho_limit")
        repeat(20) { sessions.create(user, clientInfo) }

        val error = assertThrows(ServiceException::class.java) { sessions.create(user, clientInfo) }

        assertEquals(ErrorCode.AUTH_SESSION_LIMIT_REACHED, error.errorCode)
        assertEquals(20L, repository.countActiveByUserId(user.id, Instant.now()))
    }

    @Test
    @TestTransaction
    fun rejectsSessionWhenAdminLimitReached() {
        val user = createUser(users, hasher, "bancho_admin", group = GroupIdentifier.ADMIN)
        repeat(40) { sessions.create(user, clientInfo) }

        val error = assertThrows(ServiceException::class.java) { sessions.create(user, clientInfo) }

        assertEquals(ErrorCode.AUTH_SESSION_LIMIT_REACHED, error.errorCode)
        assertEquals(40L, repository.countActiveByUserId(user.id, Instant.now()))
    }

    @Test
    @TestTransaction
    fun ignoresExpiredSessionsWhenCounting() {
        val user = createUser(users, hasher, "bancho_exp")
        repeat(20) { sessions.create(user, clientInfo) }
        repository.list("userId", user.id).forEach { it.expiresAt = Instant.now().minusSeconds(1) }

        sessions.create(user, clientInfo)

        assertEquals(21, repository.list("userId", user.id).size)
        assertEquals(1L, repository.countActiveByUserId(user.id, Instant.now()))
    }
}
