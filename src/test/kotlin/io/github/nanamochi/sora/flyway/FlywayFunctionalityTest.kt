package io.github.nanamochi.sora.flyway

import io.github.nanamochi.sora.user.repository.UserRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@QuarkusTest
@TestProfile(FlywayFunctionalityProfile::class)
@DisplayName("Tests flyway extension")
class FlywayFunctionalityTest {
    @Inject lateinit var flyway: Flyway

    @Inject lateinit var users: UserRepository

    @Test
    @DisplayName("Migrates a schema correctly using integrated instance")
    fun testFlywayQuarkusFunctionality() {
        assertEquals("1", flyway.info().current().version.toString())
        assertEquals(0L, users.count())
    }
}

class FlywayFunctionalityProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> =
        mapOf(
            "quarkus.datasource.jdbc.url" to
                "jdbc:h2:mem:sora-flyway;MODE=MariaDB;DB_CLOSE_DELAY=-1",
            "quarkus.flyway.enabled" to "true",
            "quarkus.flyway.migrate-at-start" to "true",
            "quarkus.hibernate-orm.schema-management.strategy" to "validate",
        )
}
