package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.model.BanchoSession
import io.github.nanamochi.sora.bancho.repository.BanchoSessionRepository
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.Tokens
import io.github.nanamochi.sora.user.model.User
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.Instant

private const val PING_TIMEOUT_SECONDS = 300L
private const val MAX_ACTIVE_SESSIONS = 20L
private const val MAX_ACTIVE_SESSIONS_ADMIN = 40L

@ApplicationScoped
class BanchoSessionService(private val sessions: BanchoSessionRepository) {
    @Transactional
    fun create(user: User, clientInfo: ClientInfo): BanchoSession {
        val now = Instant.now()
        val limit = if (user.isAdmin) MAX_ACTIVE_SESSIONS_ADMIN else MAX_ACTIVE_SESSIONS
        if (sessions.countActiveByUserId(user.id, now) >= limit) {
            throw ServiceException(ErrorCode.AUTH_SESSION_LIMIT_REACHED)
        }

        val session =
            BanchoSession().apply {
                userId = user.id
                accessToken = Tokens.randomToken()
                createdAt = now
                expiresAt = now.plusSeconds(PING_TIMEOUT_SECONDS)
                ipAddress = clientInfo.ipAddress
                userAgent = clientInfo.userAgent
            }
        sessions.persist(session)
        return session
    }

    @Transactional
    fun extendSession(token: String): BanchoSession {
        val session =
            sessions.findByToken(token) ?: throw ServiceException(ErrorCode.AUTH_SESSION_NOT_FOUND)
        val now = Instant.now()
        if (!session.expiresAt.isAfter(now)) {
            throw ServiceException(ErrorCode.AUTH_SESSION_EXPIRED)
        }

        session.expiresAt = now.plusSeconds(PING_TIMEOUT_SECONDS)
        return session
    }

    @Transactional
    fun revoke(token: String) {
        sessions.deleteByToken(token)
    }
}
