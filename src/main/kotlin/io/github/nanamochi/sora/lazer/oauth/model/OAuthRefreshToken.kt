package io.github.nanamochi.sora.lazer.oauth.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "oauth_refresh_tokens")
class OAuthRefreshToken : PanacheEntityBase {
    @Id @Column(length = 36) var id: String = UUID.randomUUID().toString()

    @Column(name = "access_token_id", length = 36) lateinit var accessTokenId: String

    @Column(name = "refresh_token", length = 64) lateinit var refreshToken: String

    var revoked: Boolean = false

    @Column(name = "created_at") lateinit var createdAt: Instant

    @Column(name = "expires_at") lateinit var expiresAt: Instant
}
