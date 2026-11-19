package io.github.nanamochi.sora.lazer.oauth.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "oauth_access_tokens")
class OAuthAccessToken : PanacheEntityBase {
    @Id @Column(length = 36) var id: String = UUID.randomUUID().toString()

    @Column(name = "user_id") var userId: Int = 0

    @Column(name = "client_id") var clientId: Int = 0

    @Column(name = "access_token", length = 64) lateinit var accessToken: String

    @Column(length = 255) var scopes: String = "*"

    var revoked: Boolean = false

    @Column(name = "created_at") lateinit var createdAt: Instant

    @Column(name = "expires_at") lateinit var expiresAt: Instant
}
