package io.github.nanamochi.sora.lazer.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserStatisticsResponse(
    val rankedScore: Long = 0,
    val totalScore: Long = 0,
    val pp: Float = 0f,
    val globalRank: Int? = null,
    val countryRank: Int? = null,
    val isRanked: Boolean = false,
    val rankHistory: RankHistoryResponse? = null,
    val hitAccuracy: Float = 100f,
    val playCount: Int = 0,
    val playTime: Int = 0,
    val totalHits: Long = 0,
    val maximumCombo: Int = 0,
    val replaysWatched: Int = 0,
    val gradeCounts: GradeCountsResponse = GradeCountsResponse(),
    val level: LevelResponse = LevelResponse(),
)

data class GradeCountsResponse(
    val ss: Int = 0,
    val ssh: Int = 0,
    val s: Int = 0,
    val sh: Int = 0,
    val a: Int = 0,
)

data class LevelResponse(val current: Int = 1, val progress: Int = 0)

data class RankHistoryResponse(val mode: String, val data: List<Int> = emptyList())
