package io.github.nanamochi.sora.bancho.repository

import io.github.nanamochi.sora.bancho.model.BanchoSession
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class BanchoSessionRepository : PanacheRepositoryBase<BanchoSession, String> {
    fun findByToken(token: String): BanchoSession? = find("accessToken", token).firstResult()

    fun countActiveByUserId(userId: Int, now: Instant): Long =
        count("userId = ?1 and expiresAt > ?2", userId, now)

    fun deleteByToken(token: String) {
        delete("accessToken", token)
    }
}
