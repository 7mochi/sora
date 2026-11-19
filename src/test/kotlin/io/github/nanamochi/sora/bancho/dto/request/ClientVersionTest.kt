package io.github.nanamochi.sora.bancho.dto.request

import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClientVersionTest {
    @Test
    fun parsesBuildAndStream() {
        val version = ClientVersion.from("b20250306")
        assertEquals(20250306, version.build)
        assertEquals("stable", version.stream)
        assertEquals("cuttingedge", ClientVersion.from("b20250306cuttingedge").stream)
        assertEquals("beta", ClientVersion.from("b20250306beta").stream)
        assertEquals("tourney", ClientVersion.from("b20250306tourney").stream)
        assertEquals("cuttingedge", ClientVersion.from("b20250306CUTTINGEDGE").stream)
    }

    @Test
    fun rejectsInvalidVersions() {
        assertThrows(IllegalArgumentException::class.java) { ClientVersion.from("20250306") }
        assertThrows(IllegalArgumentException::class.java) { ClientVersion.from("b") }
    }

    @Test
    fun marksOldBuildNumbers() {
        assertTrue(ClientVersion.from("b340").isTooOld(LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun stableVersionsAreOldAfterTwentyFourMonths() {
        val version = ClientVersion.from("b20250306")

        assertFalse(version.isTooOld(LocalDate.of(2027, 3, 6)))
        assertTrue(version.isTooOld(LocalDate.of(2027, 3, 7)))
    }

    @Test
    fun cuttingEdgeVersionsAreOldAfterTwelveMonths() {
        val version = ClientVersion.from("b20250306cuttingedge")

        assertFalse(version.isTooOld(LocalDate.of(2026, 3, 6)))
        assertTrue(version.isTooOld(LocalDate.of(2026, 3, 7)))
    }

    @Test
    fun ignoresInvalidBuildDates() {
        assertFalse(ClientVersion.from("b20251340").isTooOld(LocalDate.of(2030, 1, 1)))
    }
}
