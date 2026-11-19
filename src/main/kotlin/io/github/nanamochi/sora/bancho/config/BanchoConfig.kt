package io.github.nanamochi.sora.bancho.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "sora.bancho")
interface BanchoConfig {
    fun welcomeMessage(): String
}
