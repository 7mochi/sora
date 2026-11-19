package io.github.nanamochi.sora.lazer.security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.credential.TokenCredential
import io.quarkus.security.identity.IdentityProviderManager
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.AuthenticationRequest
import io.quarkus.vertx.http.runtime.security.ChallengeData
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport
import io.smallrye.mutiny.Uni
import io.vertx.ext.web.RoutingContext
import jakarta.enterprise.context.ApplicationScoped
import java.util.Optional

@ApplicationScoped
class LazerBearerAuthenticationMechanism : HttpAuthenticationMechanism {
    override fun authenticate(
        context: RoutingContext,
        identityProviderManager: IdentityProviderManager,
    ): Uni<SecurityIdentity> {
        val header =
            context.request().getHeader("Authorization")
                ?: return Uni.createFrom().optional(Optional.empty())
        if (!header.startsWith("Bearer ", ignoreCase = true)) {
            return Uni.createFrom().optional(Optional.empty())
        }

        val token = header.substring(7).trim()
        if (token.isEmpty()) return Uni.createFrom().failure(AuthenticationFailedException())

        val request = LazerTokenAuthenticationRequest(TokenCredential(token, "Bearer"))
        return identityProviderManager.authenticate(request)
    }

    override fun getChallenge(context: RoutingContext): Uni<ChallengeData> =
        Uni.createFrom().item(ChallengeData(401, "WWW-Authenticate", "Bearer"))

    override fun getCredentialTypes(): Set<Class<out AuthenticationRequest>> =
        setOf(LazerTokenAuthenticationRequest::class.java)

    override fun getCredentialTransport(context: RoutingContext): Uni<HttpCredentialTransport> =
        Uni.createFrom()
            .item(HttpCredentialTransport(HttpCredentialTransport.Type.AUTHORIZATION, "Bearer"))

    override fun sendChallenge(context: RoutingContext): Uni<Boolean> {
        context.response().statusCode = 401
        context.response().headers().set("WWW-Authenticate", "Bearer")
        context.response().headers().set("Content-Type", "application/json")
        context
            .response()
            .end("""{"error":"Unauthorized","error_description":"Invalid or missing token"}""")
        return Uni.createFrom().item(true)
    }

    override fun getPriority(): Int = 2000
}
