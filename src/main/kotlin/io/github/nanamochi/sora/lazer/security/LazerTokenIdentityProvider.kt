package io.github.nanamochi.sora.lazer.security

import io.github.nanamochi.sora.shared.error.ServiceException
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class LazerTokenIdentityProvider(private val resolver: LazerIdentityResolver) :
    IdentityProvider<LazerTokenAuthenticationRequest> {
    override fun getRequestType(): Class<LazerTokenAuthenticationRequest> =
        LazerTokenAuthenticationRequest::class.java

    override fun authenticate(
        request: LazerTokenAuthenticationRequest,
        context: AuthenticationRequestContext,
    ): Uni<SecurityIdentity> =
        context.runBlocking {
            try {
                resolver.resolve(request.token.token)
            } catch (_: ServiceException) {
                throw AuthenticationFailedException()
            }
        }
}
