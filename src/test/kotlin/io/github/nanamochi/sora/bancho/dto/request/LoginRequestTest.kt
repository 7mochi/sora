package io.github.nanamochi.sora.bancho.dto.request

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoginRequestTest {
    private val passwordMd5 = "5f4dcc3b5aa765d61d8327deb882cf99" // password

    @Test
    fun parsesFullLoginBody() {
        val request =
            LoginRequest.from(
                """
                player
                $passwordMd5
                b20250306|-5|1|osu-path:adapter:adapter-md5:uninstall:disk|1
                """
                    .trimIndent()
                    .toByteArray()
            )

        assertEquals("player", request.username)
        assertEquals(passwordMd5, request.passwordMd5)
        assertEquals(ClientVersion(20250306, "stable"), request.clientVersion)
        assertEquals(-5, request.utcOffset)
        assertTrue(request.displayCity)
        assertTrue(request.pmPrivate)
        assertEquals("osu-path", request.clientHashes.osuPathMd5)
        assertEquals("adapter", request.clientHashes.adapters)
        assertEquals("adapter-md5", request.clientHashes.adaptersMd5)
        assertEquals("uninstall", request.clientHashes.uninstallMd5)
        assertEquals("disk", request.clientHashes.diskSignatureMd5)
    }

    @Test
    fun parsesBodyWithoutTrailingNewline() {
        val body = "player\n$passwordMd5\nb20250306cuttingedge|0|0|a:adapter:c:d:e|0\n"

        val request = LoginRequest.from(body.trimEnd('\n').toByteArray())

        assertEquals("player", request.username)
        assertEquals("cuttingedge", request.clientVersion.stream)
    }

    @Test
    fun parsesClientHashesWithTrailingColon() {
        val request =
            LoginRequest.from(
                "player\n$passwordMd5\nb20250306|0|0|a:adapter:c:d:e:|0\n".toByteArray()
            )

        assertEquals("a", request.clientHashes.osuPathMd5)
        assertEquals("e", request.clientHashes.diskSignatureMd5)
    }

    @Test
    fun rejectsMissingClientHashes() {
        val invalid =
            """
            player
            $passwordMd5
            b20250306|0|0|one:two|0
            """
                .trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            LoginRequest.from(invalid.toByteArray())
        }
    }

    @Test
    fun rejectsMissingMetadataFields() {
        val invalid =
            """
            player
            $passwordMd5
            b20250306|0|0|a:adapter:c:d
            """
                .trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            LoginRequest.from(invalid.toByteArray())
        }
    }

    @Test
    fun rejectsInvalidUtcOffset() {
        val invalid = "player\n$passwordMd5\nb20250306|not-a-number|0|a:adapter:c:d:e|0\n"

        assertThrows(IllegalStateException::class.java) { LoginRequest.from(invalid.toByteArray()) }
    }
}
