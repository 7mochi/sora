package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_ID
import io.github.nanamochi.sora.utils.OAUTH_CLIENT_SECRET
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class OAuthResourceTest {
    @Inject lateinit var users: UserService

    @Test
    fun passwordGrantReturnsTokens() {
        val username = uniqueUsername("oauth_player")
        createUser(users, username)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", "secret123")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(200)
            .body("token_type", equalTo("Bearer"))
            .body("expires_in", equalTo(86400))
            .body("scope", equalTo("*"))
            .body("access_token", not(emptyString()))
            .body("refresh_token", not(emptyString()))
            .header("Cache-Control", equalTo("no-store"))
            .header("Pragma", equalTo("no-cache"))
    }

    @Test
    fun wrongPasswordReturnsInvalidGrant() {
        val username = uniqueUsername("wrong_password")
        createUser(users, username)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", "wrong")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_grant"))
    }

    @Test
    fun unsupportedGrantReturnsUnsupportedGrantType() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("unsupported_grant_type"))
    }

    @Test
    fun nonNumericClientIdReturnsInvalidClient() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", "not-a-number")
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", "nobody")
            .formParam("password", "secret123")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(401)
            .body("error", equalTo("invalid_client"))
    }

    @Test
    fun unknownClientReturnsInvalidClient() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", "999")
            .formParam("client_secret", "wrong")
            .formParam("username", "nobody")
            .formParam("password", "secret123")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(401)
            .body("error", equalTo("invalid_client"))
    }

    @Test
    fun wrongScopeReturnsInvalidScope() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", "nobody")
            .formParam("password", "secret123")
            .formParam("scope", "public")
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_scope"))
    }

    @Test
    fun restrictedUserCanGetToken() {
        val username = uniqueUsername("restricted_oauth")
        createUser(users, username, restricted = true)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", "secret123")
            .formParam("scope", "*")
            .post("/oauth/token")
            .then()
            .statusCode(200)
            .body("token_type", equalTo("Bearer"))
            .body("access_token", not(emptyString()))
            .body("refresh_token", not(emptyString()))
    }

    @Test
    fun refreshWithWrongScopeReturnsInvalidScope() {
        val username = uniqueUsername("refresh_scope")
        createUser(users, username)
        val refresh =
            given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", OAUTH_CLIENT_ID)
                .formParam("client_secret", OAUTH_CLIENT_SECRET)
                .formParam("username", username)
                .formParam("password", "secret123")
                .formParam("scope", "*")
                .post("/oauth/token")
                .then()
                .statusCode(200)
                .extract()
                .path<String>("refresh_token")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "refresh_token")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("refresh_token", refresh)
            .formParam("scope", "public")
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_scope"))
    }

    @Test
    fun refreshRotatesToken() {
        val username = uniqueUsername("refresh_oauth")
        createUser(users, username)

        val original =
            given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", OAUTH_CLIENT_ID)
                .formParam("client_secret", OAUTH_CLIENT_SECRET)
                .formParam("username", username)
                .formParam("password", "secret123")
                .formParam("scope", "*")
                .post("/oauth/token")
                .then()
                .statusCode(200)
                .extract()

        val oldRefresh = original.path<String>("refresh_token")
        val newRefresh =
            given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "refresh_token")
                .formParam("client_id", OAUTH_CLIENT_ID)
                .formParam("client_secret", OAUTH_CLIENT_SECRET)
                .formParam("refresh_token", oldRefresh)
                .post("/oauth/token")
                .then()
                .statusCode(200)
                .extract()
                .path<String>("refresh_token")

        assertNotEquals(oldRefresh, newRefresh)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "refresh_token")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .formParam("refresh_token", oldRefresh)
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_grant"))
    }

    @Test
    fun passwordGrantRequiresCredentials() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_request"))
    }

    @Test
    fun refreshGrantRequiresToken() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "refresh_token")
            .formParam("client_id", OAUTH_CLIENT_ID)
            .formParam("client_secret", OAUTH_CLIENT_SECRET)
            .post("/oauth/token")
            .then()
            .statusCode(400)
            .body("error", equalTo("invalid_request"))
    }
}
