package io.github.nanamochi.sora.user.model

enum class Gamemode(val value: Int, val alias: String) {
    OSU(0, "osu"),
    TAIKO(1, "taiko"),
    CATCH(2, "fruits"),
    MANIA(3, "mania");

    companion object {
        fun fromAlias(alias: String?): Gamemode? = entries.firstOrNull { it.alias == alias }

        fun fromValue(value: Int): Gamemode? = entries.firstOrNull { it.value == value }
    }
}
