package io.github.nanamochi.sora.bancho.dto.request

import jakarta.ws.rs.core.HttpHeaders

data class ClientInfo(val ipAddress: String, val userAgent: String?) {
    companion object {
        fun fromRequest(headers: HttpHeaders, remoteAddress: String?): ClientInfo =
            ClientInfo(
                ipAddress = remoteAddress?.takeIf { it.isNotBlank() }?.take(45) ?: "127.0.0.1",
                userAgent = headers.getHeaderString("User-Agent")?.take(255),
            )
    }
}
