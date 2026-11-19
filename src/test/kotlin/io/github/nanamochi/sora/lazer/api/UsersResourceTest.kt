package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.oauthAccessToken
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class UsersResourceTest {
    @Inject lateinit var users: UserService

    @Test
    fun userModeReturnsRequestedMode() {
        val username = uniqueUsername("users_mode")
        val user = createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/users/${user.id}/taiko")
            .then()
            .statusCode(200)
            .body("playmode", equalTo("taiko"))
    }

    @Test
    fun unknownModeReturnsNotFound() {
        val username = uniqueUsername("users_bad_mode")
        val user = createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/users/${user.id}/invalid")
            .then()
            .statusCode(404)
    }

    @Test
    fun findsUserByIdAndUsername() {
        val username = uniqueUsername("lookup_player")
        val user = createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/users/${user.id}")
            .then()
            .statusCode(200)
            .body("username", equalTo(username))

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/users/@$username")
            .then()
            .statusCode(200)
            .body("id", equalTo(user.id))
    }

    @Test
    fun missingUserUsesSpecErrorShape() {
        val username = uniqueUsername("missing_lookup")
        createUser(users, username)
        val token = oauthAccessToken(username)

        given()
            .header("Authorization", "Bearer $token")
            .get("/api/v2/users/999999")
            .then()
            .statusCode(404)
            .body("error", equalTo("User not found"))
            .body("error_description", nullValue())
    }
}
