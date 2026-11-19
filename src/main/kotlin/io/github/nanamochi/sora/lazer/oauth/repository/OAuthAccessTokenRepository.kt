package io.github.nanamochi.sora.lazer.oauth.repository

import io.github.nanamochi.sora.lazer.oauth.model.OAuthAccessToken
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OAuthAccessTokenRepository : PanacheRepositoryBase<OAuthAccessToken, String> {
    fun findByToken(token: String): OAuthAccessToken? = find("accessToken", token).firstResult()

    fun revokeById(id: String) {
        update("revoked = true where id = ?1", id)
    }
}
