package io.github.nanamochi.sora.lazer.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OAuthErrorResponse(
    val error: String,
    @get:JsonProperty("error_description") val errorDescription: String,
    val hint: String? = null,
)
