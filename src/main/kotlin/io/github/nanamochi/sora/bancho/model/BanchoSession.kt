package io.github.nanamochi.sora.bancho.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "bancho_sessions")
class BanchoSession : PanacheEntityBase {
    @Id @Column(length = 36) var id: String = UUID.randomUUID().toString()

    @Column(name = "user_id") var userId: Int = 0

    @Column(name = "access_token", length = 64) lateinit var accessToken: String

    @Column(name = "created_at") lateinit var createdAt: Instant

    @Column(name = "expires_at") lateinit var expiresAt: Instant

    @Column(name = "ip_address", length = 45) lateinit var ipAddress: String

    @Column(name = "user_agent", length = 255) var userAgent: String? = null
}
