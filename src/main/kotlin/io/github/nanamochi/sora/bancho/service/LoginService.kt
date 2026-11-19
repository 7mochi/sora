package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.auth.service.AuthService
import io.github.nanamochi.sora.bancho.config.BanchoConfig
import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.dto.request.LoginRequest
import io.github.nanamochi.sora.bancho.packet.LoginPackets
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.user.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@ApplicationScoped
class LoginService(
    private val authService: AuthService,
    private val banchoSessions: BanchoSessionService,
    private val userService: UserService,
    private val settings: BanchoConfig,
) {
    fun login(body: ByteArray, clientInfo: ClientInfo): Response {
        val request =
            try {
                LoginRequest.from(body)
            } catch (_: Exception) {
                return failure(-1, "Invalid login request")
            }

        if (request.clientVersion.isTooOld()) {
            return failure(-2, "Client is too old")
        }

        return try {
            val user =
                authService.authenticate(request.username, Credential.Md5(request.passwordMd5))
            userService.updateLatestActivity(user.id)
            val session = banchoSessions.create(user, clientInfo)

            Response.ok(LoginPackets.success(user, request, settings.welcomeMessage()))
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("cho-token", session.accessToken)
                .build()
        } catch (error: ServiceException) {
            when (error.errorCode) {
                ErrorCode.USER_INVALID_CREDENTIALS -> failure(-1, "Invalid username or password")
                ErrorCode.AUTH_SESSION_LIMIT_REACHED -> failure(-2, error.message)
                else -> failure(-5, "An internal error occurred")
            }
        } catch (_: Exception) {
            failure(-5, "An internal error occurred")
        }
    }

    private fun failure(code: Int, message: String): Response =
        Response.ok(LoginPackets.failure(code, message))
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("cho-token", "no")
            .build()
}
