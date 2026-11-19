package io.github.nanamochi.sora.lazer.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "sora.lazer")
interface LazerConfig {
    fun clientId(): Int

    fun clientSecret(): String
}
