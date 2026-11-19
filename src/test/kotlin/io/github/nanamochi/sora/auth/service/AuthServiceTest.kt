package io.github.nanamochi.sora.auth.service

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class AuthServiceTest {
    @Inject lateinit var authService: AuthService
    @Inject lateinit var users: UserRepository
    @Inject lateinit var hasher: PasswordHasher
    @Inject lateinit var userService: UserService

    @Test
    @TestTransaction
    fun authenticatesMd5AfterRegistration() {
        val username = uniqueUsername("fixed")
        val user = createUser(userService, username)

        val authenticated =
            authService.authenticate(username, Credential.Md5(hasher.md5Hex("secret123")))

        assertEquals(user.username, authenticated.username)
    }

    @Test
    @TestTransaction
    fun authenticatesPlainPassword() {
        val user = createUser(users, hasher, "plain_player")

        val authenticated = authService.authenticate("plain_player", Credential.Plain("secret"))

        assertEquals(user.username, authenticated.username)
    }

    @Test
    @TestTransaction
    fun authenticatesBySafeUsername() {
        val user = createUser(users, hasher, "Player One")

        val authenticated = authService.authenticate("player_one", Credential.Plain("secret"))

        assertEquals(user.username, authenticated.username)
    }

    @Test
    @TestTransaction
    fun authenticatesByEmail() {
        val user = createUser(users, hasher, "email_player")

        val authenticated =
            authService.authenticate("EMAIL_PLAYER@Example.COM", Credential.Plain("secret"))

        assertEquals(user.username, authenticated.username)
    }

    @Test
    @TestTransaction
    fun rejectsUnknownUser() {
        val error =
            assertThrows(ServiceException::class.java) {
                authService.authenticate("nobody_here", Credential.Plain("secret"))
            }

        assertEquals(ErrorCode.USER_INVALID_CREDENTIALS, error.errorCode)
    }

    @Test
    @TestTransaction
    fun rejectsWrongPassword() {
        createUser(users, hasher, "wrong_player")

        val error =
            assertThrows(ServiceException::class.java) {
                authService.authenticate("wrong_player", Credential.Plain("wrong"))
            }

        assertEquals(ErrorCode.USER_INVALID_CREDENTIALS, error.errorCode)
    }

    @Test
    @TestTransaction
    fun allowsRestrictedUserToAuthenticate() {
        val user = createUser(users, hasher, "restricted_user", restricted = true)

        val authenticated = authService.authenticate("restricted_user", Credential.Plain("secret"))

        assertEquals(user.username, authenticated.username)
        assertTrue(authenticated.isRestricted)
    }

    @Test
    @TestTransaction
    fun rejectsUserWithBlankEmail() {
        val user = createUser(users, hasher, "blank_email")
        user.email = ""

        val error =
            assertThrows(ServiceException::class.java) {
                authService.authenticate("blank_email", Credential.Plain("secret"))
            }

        assertEquals(ErrorCode.USER_INVALID_CREDENTIALS, error.errorCode)
    }
}
