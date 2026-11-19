package io.github.nanamochi.sora.auth.service

import io.github.nanamochi.sora.auth.model.Credential
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class AuthService(private val passwordHasher: PasswordHasher, private val users: UserRepository) {
    @Transactional
    fun authenticate(identifier: String, credential: Credential): User {
        val user = users.findByUsernameOrEmail(identifier)
        if (
            user == null ||
                user.email.isBlank() ||
                !passwordMatches(credential, user.passwordBcrypt)
        ) {
            throw ServiceException(ErrorCode.USER_INVALID_CREDENTIALS)
        }

        return user
    }

    private fun passwordMatches(credential: Credential, bcrypt: String): Boolean {
        return when (credential) {
            is Credential.Plain -> passwordHasher.matchesPlain(credential.value, bcrypt)
            is Credential.Md5 -> passwordHasher.matchesMd5(credential.value, bcrypt)
        }
    }
}
