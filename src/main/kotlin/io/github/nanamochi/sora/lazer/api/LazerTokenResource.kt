package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.lazer.oauth.service.OAuthTokenService
import io.github.nanamochi.sora.lazer.security.soraToken
import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response

@Path("/api/v2/oauth/tokens/current")
@Authenticated
class LazerTokenResource(
    private val identity: SecurityIdentity,
    private val oauthTokens: OAuthTokenService,
) {
    @DELETE
    fun revokeCurrent(): Response {
        oauthTokens.revokeAccessToken(identity.soraToken())
        return Response.noContent().build()
    }
}
