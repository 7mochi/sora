package io.github.nanamochi.sora.user.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User : PanacheEntityBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int = 0

    @Column(length = 15, nullable = false, unique = true) lateinit var username: String

    @Column(name = "safe_username", length = 15, nullable = false, unique = true)
    lateinit var safeUsername: String

    @Column(length = 254, nullable = false, unique = true) lateinit var email: String

    @Column(name = "password_bcrypt", length = 60, nullable = false)
    lateinit var passwordBcrypt: String

    @Enumerated(EnumType.STRING)
    @Column(length = 2, nullable = false)
    var country: CountryCode = CountryCode.XX

    @Column(name = "group_identifier", length = 32, nullable = false)
    var groupIdentifier: String = GroupIdentifier.DEFAULT.identifier

    @Column(name = "osu_subscriber", nullable = false) var osuSubscriber: Boolean = false

    @Column(name = "preferred_mode", nullable = false) var preferredMode: Int = 0

    @Column(name = "registration_time", nullable = false) lateinit var registrationTime: Instant

    @Column(name = "latest_activity", nullable = false) lateinit var latestActivity: Instant

    @Column(name = "silence_end") var silenceEnd: Instant? = null

    @Column(name = "restriction_time") var restrictionTime: Instant? = null

    var group: GroupIdentifier
        get() = GroupIdentifier.from(groupIdentifier) ?: GroupIdentifier.DEFAULT
        set(value) {
            groupIdentifier = value.identifier
        }

    val isRestricted: Boolean
        get() = restrictionTime != null

    val isSupporter: Boolean
        get() = osuSubscriber

    val isAdmin: Boolean
        get() = group == GroupIdentifier.ADMIN

    val isDeveloper: Boolean
        get() = group == GroupIdentifier.DEV

    val isModerator: Boolean
        get() = group == GroupIdentifier.GMT || group == GroupIdentifier.NAT

    val clientPrivileges: Int
        get() {
            var bits = ClientPrivileges.PLAYER.bit
            if (isSupporter) bits = bits or ClientPrivileges.SUPPORTER.bit
            if (isModerator) bits = bits or ClientPrivileges.MODERATOR.bit
            if (isAdmin) bits = bits or ClientPrivileges.OWNER.bit
            if (isDeveloper) bits = bits or ClientPrivileges.DEVELOPER.bit
            return bits
        }

    val isSilenced: Boolean
        get() = silenceEnd?.isAfter(Instant.now()) ?: false
}

private val SAFE_USERNAME_PATTERN = Regex("[^a-z0-9_ ]")

internal fun safeUsername(username: String): String =
    username.trim().lowercase().replace(SAFE_USERNAME_PATTERN, "").replace(" ", "_")
