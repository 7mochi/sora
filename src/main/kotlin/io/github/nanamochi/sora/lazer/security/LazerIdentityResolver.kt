package io.github.nanamochi.sora.lazer.security

import io.github.nanamochi.sora.lazer.oauth.service.OAuthTokenService
import io.github.nanamochi.sora.user.service.UserService
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.runtime.QuarkusPrincipal
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class LazerIdentityResolver(
    private val oauthTokens: OAuthTokenService,
    private val userService: UserService,
) {
    @Transactional
    fun resolve(token: String): SecurityIdentity {
        val accessToken = oauthTokens.validateAccessToken(token)
        val user = userService.findById(accessToken.userId) ?: throw AuthenticationFailedException()

        return QuarkusSecurityIdentity.builder()
            .setPrincipal(QuarkusPrincipal(user.username))
            .addRole("USER")
            .addAttribute("sora.user", user)
            .addAttribute("sora.token", token)
            .build()
    }
}
