package io.github.nanamochi.sora.user.repository

import io.github.nanamochi.sora.user.model.Gamemode
import io.github.nanamochi.sora.user.model.UserStatistics
import io.github.nanamochi.sora.user.model.UserStatisticsId
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserStatisticsRepository : PanacheRepositoryBase<UserStatistics, UserStatisticsId> {
    fun findByUserId(userId: Int): List<UserStatistics> = list("userId", userId)

    fun find(userId: Int, mode: Gamemode): UserStatistics? =
        find("userId = ?1 and mode = ?2", userId, mode).firstResult()
}
