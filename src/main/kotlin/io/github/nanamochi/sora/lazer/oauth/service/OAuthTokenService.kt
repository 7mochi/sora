package io.github.nanamochi.sora.lazer.oauth.service

import io.github.nanamochi.sora.lazer.config.LazerConfig
import io.github.nanamochi.sora.lazer.oauth.model.OAuthAccessToken
import io.github.nanamochi.sora.lazer.oauth.model.OAuthRefreshToken
import io.github.nanamochi.sora.lazer.oauth.model.OAuthTokens
import io.github.nanamochi.sora.lazer.oauth.repository.OAuthAccessTokenRepository
import io.github.nanamochi.sora.lazer.oauth.repository.OAuthRefreshTokenRepository
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.Tokens
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val LAZER_ACCESS_TTL = Duration.ofDays(1)
private val LAZER_REFRESH_TTL = Period.ofMonths(3)

@ApplicationScoped
class OAuthTokenService(
    private val accessTokens: OAuthAccessTokenRepository,
    private val refreshTokens: OAuthRefreshTokenRepository,
    private val config: LazerConfig,
) {
    @Transactional
    fun create(userId: Int): OAuthTokens {
        val now = Instant.now()
        val access =
            OAuthAccessToken().apply {
                this.userId = userId
                clientId = config.clientId()
                accessToken = Tokens.randomToken()
                scopes = "*"
                createdAt = now
                expiresAt = now.plus(LAZER_ACCESS_TTL)
            }
        accessTokens.persist(access)

        val refresh =
            OAuthRefreshToken().apply {
                accessTokenId = access.id
                refreshToken = Tokens.randomToken()
                createdAt = now
                expiresAt = now.plusPeriod(LAZER_REFRESH_TTL)
            }
        refreshTokens.persist(refresh)

        return OAuthTokens(access.accessToken, refresh.refreshToken, LAZER_ACCESS_TTL.seconds)
    }

    fun validateAccessToken(token: String): OAuthAccessToken {
        val access =
            accessTokens.findByToken(token)
                ?: throw ServiceException(ErrorCode.AUTH_SESSION_NOT_FOUND)
        if (access.revoked) throw ServiceException(ErrorCode.AUTH_SESSION_NOT_FOUND)
        if (!access.expiresAt.isAfter(Instant.now())) {
            throw ServiceException(ErrorCode.AUTH_SESSION_EXPIRED)
        }

        return access
    }

    @Transactional
    fun rotateRefreshToken(refreshToken: String): OAuthTokens {
        val refresh =
            refreshTokens.findByTokenForUpdate(refreshToken)
                ?: throw ServiceException(ErrorCode.AUTH_INVALID_GRANT)
        if (refresh.revoked || !refresh.expiresAt.isAfter(Instant.now())) {
            throw ServiceException(ErrorCode.AUTH_INVALID_GRANT)
        }

        val access =
            accessTokens.findById(refresh.accessTokenId)
                ?: throw ServiceException(ErrorCode.AUTH_INVALID_GRANT)
        if (access.revoked) throw ServiceException(ErrorCode.AUTH_INVALID_GRANT)

        val userId = access.userId
        accessTokens.revokeById(access.id)
        refreshTokens.revokeById(refresh.id)
        accessTokens.getEntityManager().refresh(access)
        refreshTokens.getEntityManager().refresh(refresh)

        return create(userId)
    }

    @Transactional
    fun revokeAccessToken(token: String) {
        val access = accessTokens.findByToken(token) ?: return
        accessTokens.revokeById(access.id)
        refreshTokens.revokeByAccessTokenId(access.id)
        accessTokens.getEntityManager().refresh(access)
    }

    private fun Instant.plusPeriod(period: Period): Instant =
        ZonedDateTime.ofInstant(this, ZoneOffset.UTC).plus(period).toInstant()
}
