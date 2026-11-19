package io.github.nanamochi.sora.user.model

import io.github.nanamochi.chio.packet.types.Country
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CountryCodeTest {
    @Test
    fun idsMatchChioOrdinals() {
        CountryCode.entries.forEach {
            assertEquals(Country.fromAcronym(it.name).ordinal, it.id, it.name)
        }
    }

    @Test
    fun unknownCountryIsZero() {
        assertEquals(0, CountryCode.XX.id)
        assertEquals(CountryCode.XX, CountryCode.fromCode("unknown"))
    }
}
