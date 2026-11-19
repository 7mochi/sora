package io.github.nanamochi.sora.shared.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "sora.server")
interface ServerConfig {
    fun publicUrl(): String
}
