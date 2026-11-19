package io.github.nanamochi.sora.shared.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiError(
    val error: String,
    @get:JsonProperty("error_description") val errorDescription: String? = null,
)
