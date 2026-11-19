package io.github.nanamochi.sora.lazer.oauth.model

data class OAuthTokens(val accessToken: String, val refreshToken: String, val expiresIn: Long)
