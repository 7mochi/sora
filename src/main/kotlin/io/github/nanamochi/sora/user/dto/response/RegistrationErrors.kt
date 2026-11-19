package io.github.nanamochi.sora.user.dto.response

data class RegistrationErrors(
    val username: List<String> = emptyList(),
    val email: List<String> = emptyList(),
    val password: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = username.isEmpty() && email.isEmpty() && password.isEmpty()
}
