package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_ID
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_SECRET
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.oauthLogin
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class LazerTokenResourceTest {
    @Inject lateinit var users: UserService

    @Test
    fun revokeCurrentInvalidatesAccessAndRefreshTokens() {
        val username = uniqueUsername("revoke")
        createUser(users, username)
        val tokens = oauthLogin(username)

        given()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .delete("/api/v2/oauth/tokens/current")
            .then()
            .statusCode(204)

        given()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .get("/api/v2/me")
            .then()
            .statusCode(401)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "refresh_token")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("refresh_token", tokens.refreshToken)
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_grant"))
    }

    @Test
    fun revokeWithoutTokenIsUnauthorized() {
        given().delete("/api/v2/oauth/tokens/current").then().statusCode(401)
    }
}
