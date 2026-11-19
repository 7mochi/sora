package io.github.nanamochi.sora.bancho.dto.request

data class LoginRequest(
    val username: String,
    val passwordMd5: String,
    val clientVersion: ClientVersion,
    val utcOffset: Int,
    val displayCity: Boolean,
    val pmPrivate: Boolean,
    val clientHashes: ClientHashes,
) {
    companion object {
        fun from(body: ByteArray): LoginRequest {
            val text = body.decodeToString().trimEnd('\n')

            val lines = text.split("\n", limit = 3)
            require(lines.size == 3) { "Login body requires three lines" }

            val remainder = lines[2].split("|", limit = 5)
            require(remainder.size == 5) { "Login body requires five metadata fields" }

            val clientVersion = ClientVersion.from(remainder[0])
            val utcOffset = remainder[1].toIntOrNull() ?: error("Invalid UTC offset")

            return LoginRequest(
                username = lines[0],
                passwordMd5 = lines[1],
                clientVersion = clientVersion,
                utcOffset = utcOffset,
                displayCity = remainder[2] == "1",
                pmPrivate = remainder[4] == "1",
                clientHashes = ClientHashes.from(remainder[3]),
            )
        }
    }
}

data class ClientHashes(
    val osuPathMd5: String,
    val adapters: String,
    val adaptersMd5: String,
    val uninstallMd5: String,
    val diskSignatureMd5: String,
) {
    companion object {
        fun from(raw: String): ClientHashes {
            val parts = raw.trimEnd(':').split(":", limit = 5)
            require(parts.size == 5) { "Login body requires five client hashes" }

            return ClientHashes(
                osuPathMd5 = parts[0],
                adapters = parts[1],
                adaptersMd5 = parts[2],
                uninstallMd5 = parts[3],
                diskSignatureMd5 = parts[4],
            )
        }
    }
}
