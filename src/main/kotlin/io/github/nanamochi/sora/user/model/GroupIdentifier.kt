package io.github.nanamochi.sora.user.model

enum class GroupIdentifier(val identifier: String) {
    ADMIN("admin"),
    ALUMNI("alumni"),
    ANNOUNCE("announce"),
    BNG("bng"),
    BNG_LIMITED("bng_limited"),
    BOT("bot"),
    DEFAULT("default"),
    DEV("dev"),
    GMT("gmt"),
    LOVED("loved"),
    NAT("nat"),
    NO_PROFILE("no_profile");

    companion object {
        fun from(identifier: String): GroupIdentifier? =
            entries.firstOrNull { it.identifier == identifier }
    }
}
