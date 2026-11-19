package io.github.nanamochi.sora.lazer.dto

import java.time.Instant
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponse(
    val id: Int,
    val username: String,
    val avatarUrl: String,
    val countryCode: String,
    val isActive: Boolean,
    val isBot: Boolean,
    val isSupporter: Boolean,
    val isRestricted: Boolean,
    val playmode: String,
    val playstyle: String? = null,
    val joinDate: Instant,
    val lastVisit: Instant,
    val statistics: UserStatisticsResponse,
)
