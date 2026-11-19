package io.github.nanamochi.sora.user.repository

import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.model.safeUsername
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class UserRepository : PanacheRepositoryBase<User, Int> {
    fun findBySafeUsername(safeUsername: String): User? =
        find("safeUsername", safeUsername).firstResult()

    fun findByEmail(email: String): User? =
        find("lower(email) = ?1", email.lowercase()).firstResult()

    fun findByUsernameOrEmail(identifier: String): User? {
        val value = identifier.trim().removePrefix("@").lowercase()
        return find("safeUsername = ?1 or lower(email) = ?2", safeUsername(value), value)
            .firstResult()
    }

    fun updateLatestActivity(userId: Int, now: Instant) {
        update("latestActivity = ?1 where id = ?2", now, userId)
    }
}
