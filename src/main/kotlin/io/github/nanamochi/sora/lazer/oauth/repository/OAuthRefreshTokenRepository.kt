package io.github.nanamochi.sora.lazer.oauth.repository

import io.github.nanamochi.sora.lazer.oauth.model.OAuthRefreshToken
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.LockModeType

@ApplicationScoped
class OAuthRefreshTokenRepository : PanacheRepositoryBase<OAuthRefreshToken, String> {
    fun findByToken(token: String): OAuthRefreshToken? = find("refreshToken", token).firstResult()

    fun findByTokenForUpdate(token: String): OAuthRefreshToken? =
        find("refreshToken", token).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult()

    fun revokeById(id: String) {
        update("revoked = true where id = ?1", id)
    }

    fun revokeByAccessTokenId(accessTokenId: String) {
        update("revoked = true where accessTokenId = ?1", accessTokenId)
    }
}
