package io.github.nanamochi.sora.utils

import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.model.GroupIdentifier
import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.model.safeUsername as normalizeSafeUsername
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.user.service.UserService
import java.time.Instant
import java.util.UUID

fun uniqueUsername(prefix: String): String =
    "${prefix.take(6)}_${UUID.randomUUID().toString().take(6)}"

fun createUser(
    users: UserService,
    username: String,
    restricted: Boolean = false,
    osuSubscriber: Boolean = false,
    group: GroupIdentifier = GroupIdentifier.DEFAULT,
): User =
    users.create(
        username = username,
        email = "$username@example.com",
        password = "secret123",
        country = CountryCode.PE,
        group = group,
        osuSubscriber = osuSubscriber,
        restricted = restricted,
    )

fun createUser(
    users: UserRepository,
    hasher: PasswordHasher,
    username: String = "user_${UUID.randomUUID().toString().take(8)}",
    plainPassword: String = "secret",
    restricted: Boolean = false,
    group: GroupIdentifier = GroupIdentifier.DEFAULT,
): User {
    val now = Instant.now()
    val normalizedUsername = normalizeSafeUsername(username)
    val user =
        User().apply {
            this.username = username
            safeUsername = normalizedUsername
            email = "$normalizedUsername@example.com"
            passwordBcrypt = hasher.hash(plainPassword)
            country = CountryCode.PE
            this.group = group
            osuSubscriber = false
            restrictionTime = if (restricted) now else null
            preferredMode = 0
            registrationTime = now
            latestActivity = now
        }
    users.persist(user)
    return user
}
