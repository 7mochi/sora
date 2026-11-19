package io.github.nanamochi.sora.shared.security

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped
import java.security.MessageDigest
import java.util.HexFormat

@ApplicationScoped
class PasswordHasher {
    fun hash(plain: String): String {
        val password = md5Hex(plain).toCharArray()
        return try {
            BcryptUtil.bcryptHash(password)
        } finally {
            password.fill('\u0000')
        }
    }

    fun matchesPlain(plain: String, bcrypt: String): Boolean = verify(md5Hex(plain), bcrypt)

    fun matchesMd5(md5Hex: String, bcrypt: String): Boolean = verify(md5Hex.lowercase(), bcrypt)

    fun md5Hex(value: String): String =
        HexFormat.of()
            .formatHex(MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8)))

    private fun verify(value: String, bcrypt: String): Boolean {
        val password = value.toCharArray()
        return try {
            BcryptUtil.matches(password, bcrypt)
        } finally {
            password.fill('\u0000')
        }
    }
}
