package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.auth.service.AuthService
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_ID
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_SECRET
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow

@QuarkusTest
class OAuthResourceErrorTest {
    @InjectMock lateinit var authService: AuthService

    @Test
    fun mapsUnexpectedAuthenticationErrors() {
        doThrow(ServiceException(ErrorCode.USER_NOT_FOUND))
            .`when`(authService)
            .authenticate("missing_user", Credential.Plain("secret123"))

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", "missing_user")
            .formParam("password", "secret123")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(404)
            .body("error", equalTo("user_not_found"))
    }
}
