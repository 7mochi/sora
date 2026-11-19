package io.github.nanamochi.sora.bancho.dto.request

import java.time.DateTimeException
import java.time.LocalDate

private val VERSION_PATTERN = Regex("^b(\\d+)(.*)$")

data class ClientVersion(val build: Int, val stream: String) {
    fun isTooOld(today: LocalDate = LocalDate.now()): Boolean {
        if (build < 20_000_000) return true

        val year = build / 10_000
        val month = (build / 100) % 100
        val day = build % 100
        val date =
            try {
                LocalDate.of(year, month, day)
            } catch (_: DateTimeException) {
                return false
            }
        val months = if (stream == "cuttingedge") 12L else 24L

        return date.plusMonths(months).isBefore(today)
    }

    companion object {
        fun from(raw: String): ClientVersion {
            val match =
                VERSION_PATTERN.matchEntire(raw.trim())
                    ?: throw IllegalArgumentException("Invalid version")
            val suffix = match.groupValues[2].lowercase()
            val stream =
                when {
                    suffix.contains("cuttingedge") -> "cuttingedge"
                    suffix.contains("beta") -> "beta"
                    suffix.contains("tourney") -> "tourney"
                    else -> "stable"
                }

            return ClientVersion(match.groupValues[1].toInt(), stream)
        }
    }
}
