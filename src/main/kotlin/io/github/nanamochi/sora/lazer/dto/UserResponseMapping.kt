package io.github.nanamochi.sora.lazer.dto

import io.github.nanamochi.sora.user.model.Gamemode
import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.model.UserStatistics

fun User.toResponse(statistics: UserStatistics?, publicUrl: String, mode: Gamemode): UserResponse {
    val stats =
        UserStatisticsResponse(
            rankedScore = statistics?.rankedScore ?: 0,
            totalScore = statistics?.totalScore ?: 0,
            pp = statistics?.pp ?: 0f,
            globalRank = statistics?.globalRank,
            countryRank = statistics?.countryRank,
            isRanked = statistics?.globalRank != null,
            hitAccuracy = statistics?.accuracy ?: 100f,
            playCount = statistics?.playCount ?: 0,
            playTime = statistics?.playTime ?: 0,
            totalHits = statistics?.totalHits ?: 0,
            maximumCombo = statistics?.maximumCombo ?: 0,
            gradeCounts =
                GradeCountsResponse(
                    ss = statistics?.gradeSs ?: 0,
                    ssh = statistics?.gradeSsh ?: 0,
                    s = statistics?.gradeS ?: 0,
                    sh = statistics?.gradeSh ?: 0,
                    a = statistics?.gradeA ?: 0,
                ),
            level = LevelResponse(),
        )

    return UserResponse(
        id = id,
        username = username,
        avatarUrl = "${publicUrl.trimEnd('/')}/avatars/$id",
        countryCode = country.code.uppercase(),
        isActive = !isRestricted,
        isBot = false,
        isSupporter = isSupporter,
        isRestricted = isRestricted,
        playmode = mode.alias,
        joinDate = registrationTime,
        lastVisit = latestActivity,
        statistics = stats,
    )
}
