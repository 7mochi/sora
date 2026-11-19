package io.github.nanamochi.sora.auth.model

sealed interface Credential {
    // stable and lazer use different password formats
    val value: String

    data class Plain(override val value: String) : Credential

    data class Md5(override val value: String) : Credential
}
