package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.auth.service.AuthService
import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.packet.LoginPackets
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.utils.BANCHO_PASSWORD_MD5
import io.github.nanamochi.sora.utils.banchoLoginBody
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

@QuarkusTest
class LoginServiceTest {
    @InjectMock lateinit var authService: AuthService
    @Inject lateinit var loginService: LoginService

    private val body = banchoLoginBody("player")
    private val clientInfo = ClientInfo("127.0.0.1", "test")

    @Test
    fun returnsInternalErrorForUnexpectedServiceException() {
        `when`(authService.authenticate("player", Credential.Md5(BANCHO_PASSWORD_MD5)))
            .thenThrow(ServiceException(ErrorCode.USER_ALREADY_EXISTS))

        assertInternalError()
    }

    @Test
    fun returnsInternalErrorForUnexpectedException() {
        `when`(authService.authenticate("player", Credential.Md5(BANCHO_PASSWORD_MD5)))
            .thenThrow(RuntimeException("boom"))

        assertInternalError()
    }

    private fun assertInternalError() {
        val response = loginService.login(body.toByteArray(), clientInfo)

        assertEquals(200, response.status)
        assertEquals("no", response.getHeaderString("cho-token"))
        assertArrayEquals(
            LoginPackets.failure(-5, "An internal error occurred"),
            response.entity as ByteArray,
        )
    }
}
