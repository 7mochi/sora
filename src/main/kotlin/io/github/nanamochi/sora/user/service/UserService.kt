package io.github.nanamochi.sora.user.service

import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.dto.response.RegistrationErrors
import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.model.Gamemode
import io.github.nanamochi.sora.user.model.GroupIdentifier
import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.model.UserStatistics
import io.github.nanamochi.sora.user.model.safeUsername
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.user.repository.UserStatisticsRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import java.time.Instant

private val USERNAME_PATTERN = Regex("""^[A-Za-z0-9_ \[\]\-]{2,15}$""")
private val EMAIL_PATTERN = Regex("""^[^@\s]{1,200}@[^@\s.]{1,30}\.[^@.\s]{1,24}$""")
private val WEAK_PASSWORDS =
    setOf("password", "12345678", "qwerty123", "osu!osu!", "letmein", "iloveyou")

@ApplicationScoped
class UserService(
    private val users: UserRepository,
    private val statistics: UserStatisticsRepository,
    private val passwordHasher: PasswordHasher,
) {
    fun findById(id: Int): User? = users.findById(id)

    fun findByUsernameOrEmail(identifier: String): User? = users.findByUsernameOrEmail(identifier)

    fun findStatistics(userId: Int): List<UserStatistics> = statistics.findByUserId(userId)

    fun validateRegistration(
        username: String,
        email: String,
        password: String,
    ): RegistrationErrors {
        val usernameErrors = mutableListOf<String>()
        val emailErrors = mutableListOf<String>()
        val passwordErrors = mutableListOf<String>()

        val trimmedUsername = username.trim()
        if (!USERNAME_PATTERN.matches(trimmedUsername)) {
            usernameErrors += "Must be 2-15 characters in length."
        }
        if (trimmedUsername.contains(' ') && trimmedUsername.contains('_')) {
            usernameErrors += "May contain \"_\" and \" \", but not both."
        }
        if (
            usernameErrors.isEmpty() &&
                users.findBySafeUsername(safeUsername(trimmedUsername)) != null
        ) {
            usernameErrors += "Username already taken by another player."
        }

        val normalizedEmail = email.trim().lowercase()
        if (!EMAIL_PATTERN.matches(normalizedEmail)) {
            emailErrors += "Invalid email syntax."
        } else if (users.findByEmail(normalizedEmail) != null) {
            emailErrors += "Email already taken by another player."
        }

        if (password.length !in 8..32) {
            passwordErrors += "Must be 8-32 characters in length."
        }
        if (password.toSet().size <= 3) {
            passwordErrors += "Must have more than 3 unique characters."
        }
        if (password.lowercase().contains(trimmedUsername.lowercase())) {
            passwordErrors += "Password cannot contain your username."
        }
        if (password.lowercase() in WEAK_PASSWORDS) {
            passwordErrors += "That password was deemed too simple."
        }

        return RegistrationErrors(usernameErrors, emailErrors, passwordErrors)
    }

    @Transactional
    fun create(
        username: String,
        email: String,
        password: String,
        country: CountryCode,
        group: GroupIdentifier = GroupIdentifier.DEFAULT,
        osuSubscriber: Boolean = false,
        restricted: Boolean = false,
    ): User {
        val trimmedUsername = username.trim()
        val safeUsername = safeUsername(trimmedUsername)
        val normalizedEmail = email.trim().lowercase()
        val now = Instant.now()
        val user =
            User().apply {
                this.username = trimmedUsername
                this.safeUsername = safeUsername
                this.email = normalizedEmail
                passwordBcrypt = passwordHasher.hash(password)
                this.country = country
                this.group = group
                this.osuSubscriber = osuSubscriber
                restrictionTime = if (restricted) now else null
                preferredMode = Gamemode.OSU.value
                registrationTime = now
                latestActivity = now
            }
        try {
            users.persist(user)
            users.flush()
        } catch (_: PersistenceException) {
            throw ServiceException(ErrorCode.USER_ALREADY_EXISTS)
        }

        Gamemode.entries.forEach { mode ->
            statistics.persist(
                UserStatistics().apply {
                    userId = user.id
                    this.mode = mode
                }
            )
        }

        return user
    }

    @Transactional
    fun updateLatestActivity(userId: Int) {
        users.updateLatestActivity(userId, Instant.now())
    }
}
