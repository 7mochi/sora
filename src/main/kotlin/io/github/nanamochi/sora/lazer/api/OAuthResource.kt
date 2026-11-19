package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.auth.service.AuthService
import io.github.nanamochi.sora.lazer.config.LazerConfig
import io.github.nanamochi.sora.lazer.dto.OAuthErrorResponse
import io.github.nanamochi.sora.lazer.dto.TokenResponse
import io.github.nanamochi.sora.lazer.oauth.model.OAuthTokens
import io.github.nanamochi.sora.lazer.oauth.service.OAuthTokenService
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.FormParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.security.MessageDigest

@Path("/oauth/token")
class OAuthResource(
    private val authService: AuthService,
    private val oauthTokens: OAuthTokenService,
    private val config: LazerConfig,
) {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    fun token(
        @FormParam("grant_type") grantType: String?,
        @FormParam("client_id") clientId: String?,
        @FormParam("client_secret") clientSecret: String?,
        @FormParam("username") username: String?,
        @FormParam("password") password: String?,
        @FormParam("refresh_token") refreshToken: String?,
        @FormParam("scope") scope: String?,
    ): Response {
        if (!validClient(clientId, clientSecret)) {
            return oauthError("invalid_client", "Invalid client", 401)
        }

        return try {
            when (grantType) {
                "password" -> passwordGrant(username, password, scope)
                "refresh_token" -> refreshGrant(refreshToken, scope)
                else -> oauthError("unsupported_grant_type", "Unsupported grant type", 400)
            }
        } catch (error: ServiceException) {
            oauthError(error)
        }
    }

    private fun validClient(clientId: String?, clientSecret: String?): Boolean {
        if (clientId != config.clientId().toString()) return false
        val secret = clientSecret ?: return false

        return MessageDigest.isEqual(
            secret.toByteArray(Charsets.UTF_8),
            config.clientSecret().toByteArray(Charsets.UTF_8),
        )
    }

    private fun passwordGrant(username: String?, password: String?, scope: String?): Response {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return oauthError("invalid_request", "Missing username or password", 400)
        }
        if (scope != "*") return oauthError("invalid_scope", "Only * scope is allowed", 400)

        val user = authService.authenticate(username, Credential.Plain(password))
        val tokens = oauthTokens.create(user.id)
        return tokenResponse(tokens, scope)
    }

    private fun refreshGrant(refreshToken: String?, scope: String?): Response {
        if (refreshToken.isNullOrBlank()) {
            return oauthError("invalid_request", "Missing refresh token", 400)
        }
        if (scope != null && scope != "*") {
            return oauthError("invalid_scope", "Only * scope is allowed", 400)
        }

        val tokens = oauthTokens.rotateRefreshToken(refreshToken)
        return tokenResponse(tokens, "*")
    }

    private fun tokenResponse(tokens: OAuthTokens, scope: String): Response =
        Response.ok(
                TokenResponse(
                    accessToken = tokens.accessToken,
                    expiresIn = tokens.expiresIn,
                    refreshToken = tokens.refreshToken,
                    scope = scope,
                )
            )
            .header("Cache-Control", "no-store")
            .header("Pragma", "no-cache")
            .build()

    private fun oauthError(error: ServiceException): Response {
        val (code, status) =
            when (error.errorCode) {
                ErrorCode.USER_INVALID_CREDENTIALS -> "invalid_grant" to 400
                ErrorCode.AUTH_INVALID_GRANT -> "invalid_grant" to 400
                else -> error.errorCode.name.lowercase() to error.errorCode.status
            }
        return oauthError(code, error.message, status)
    }

    private fun oauthError(error: String, description: String, status: Int): Response =
        Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .header("Cache-Control", "no-store")
            .header("Pragma", "no-cache")
            .entity(OAuthErrorResponse(error, description))
            .build()
}
