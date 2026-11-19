package io.github.nanamochi.sora.utils

import io.restassured.RestAssured.given

const val OAUTH_CLIENT_ID = "5"
const val OAUTH_CLIENT_SECRET = "FGc9GAtyHzeQDshWP5Ah7dega8hJACAJpQtw6OXk"

data class OAuthTokens(val accessToken: String, val refreshToken: String)

fun oauthLogin(username: String): OAuthTokens {
    val response =
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
            .response()

    return OAuthTokens(
        accessToken = response.path("access_token"),
        refreshToken = response.path("refresh_token"),
    )
}

fun oauthAccessToken(username: String): String = oauthLogin(username).accessToken
