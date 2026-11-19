package io.github.nanamochi.sora.lazer.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserStatisticsResponseTest {
    @Test
    fun providesEmptyStatistics() {
        val statistics = UserStatisticsResponse()

        assertEquals(0, statistics.gradeCounts.a)
        assertEquals("osu", RankHistoryResponse("osu").mode)
    }
}
