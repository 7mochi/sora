package io.github.nanamochi.sora.shared.error

enum class ErrorCode(val status: Int, val message: String) {
    USER_INVALID_CREDENTIALS(400, "Invalid username or password"),
    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(409, "Username or email already exists"),
    USER_INVALID_USERNAME(400, "Invalid username"),
    USER_INVALID_EMAIL(400, "Invalid email"),
    USER_INVALID_PASSWORD(400, "Invalid password"),
    AUTH_INVALID_GRANT(400, "Invalid grant"),
    AUTH_SESSION_EXPIRED(401, "Session expired"),
    AUTH_SESSION_NOT_FOUND(401, "Session not found"),
    AUTH_SESSION_LIMIT_REACHED(
        403,
        "You have reached the max amount of logins. Please wait a few minutes or log out in other clients.",
    ),
}
