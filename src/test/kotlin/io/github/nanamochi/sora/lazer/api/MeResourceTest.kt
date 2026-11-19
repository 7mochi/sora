package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.oauthAccessToken
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class MeResourceTest {
    @Inject lateinit var users: UserService

    @Test
    fun meWithoutTokenIsUnauthorized() {
        given().get("/api/v2/me").then().statusCode(401)
    }

    @Test
    fun meReturnsAuthenticatedUser() {
        val username = uniqueUsername("me_player")
        val user = createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/me")
            .then()
            .statusCode(200)
            .body("id", equalTo(user.id))
            .body("username", equalTo(username))
            .body("playmode", equalTo("osu"))
            .body("statistics.hit_accuracy", equalTo(100.0f))
    }

    @Test
    fun meReturnsTaikoStats() {
        val username = uniqueUsername("taiko_player")
        createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/me/taiko")
            .then()
            .statusCode(200)
            .body("playmode", equalTo("taiko"))
    }

    @Test
    fun unknownModeReturnsNotFound() {
        val username = uniqueUsername("me_bad_mode")
        createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/me/invalid")
            .then()
            .statusCode(404)
    }

    @Test
    fun revokedTokenIsUnauthorized() {
        val username = uniqueUsername("revoked_player")
        createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .delete("/api/v2/oauth/tokens/current")
            .then()
            .statusCode(204)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/me")
            .then()
            .statusCode(401)
            .header("WWW-Authenticate", equalTo("Bearer"))
            .body("error", equalTo("Unauthorized"))
            .body("error_description", equalTo("Invalid or missing token"))
    }

    @Test
    fun nonBearerAuthorizationIsUnauthorized() {
        given().header("Authorization", "Basic abc").get("/api/v2/me").then().statusCode(401)
    }

    @Test
    fun emptyBearerTokenIsUnauthorized() {
        given().header("Authorization", "Bearer ").get("/api/v2/me").then().statusCode(401)
    }
}
