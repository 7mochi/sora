package io.github.nanamochi.sora.user.model

enum class ClientPrivileges(val bit: Int) {
    PLAYER(1),
    MODERATOR(1 shl 1),
    SUPPORTER(1 shl 2),
    OWNER(1 shl 3),
    DEVELOPER(1 shl 4),
    TOURNAMENT(1 shl 5),
}
