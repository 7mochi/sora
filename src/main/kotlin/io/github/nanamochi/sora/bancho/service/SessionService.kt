package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.chio.packet.inbound.ExitPacket
import io.github.nanamochi.chio.packet.inbound.PongPacket
import io.github.nanamochi.chio.protocol.Protocols
import io.github.nanamochi.sora.user.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import java.nio.ByteBuffer
import java.nio.ByteOrder

@ApplicationScoped
class SessionService(
    private val banchoSessions: BanchoSessionService,
    private val userService: UserService,
) {
    fun processPackets(token: String, body: ByteArray): ByteArray {
        val session = banchoSessions.extendSession(token)
        val protocol = Protocols.latest
        val lengthOffset = protocol.format.headerSize - 4
        var cursor = 0

        userService.updateLatestActivity(session.userId)

        while (cursor + protocol.format.headerSize <= body.size) {
            val length =
                ByteBuffer.wrap(body, cursor + lengthOffset, 4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .int
                    .toLong() and 0xffffffffL
            val end = cursor.toLong() + protocol.format.headerSize + length
            if (end > body.size) break

            val next = end.toInt()
            val packet = body.copyOfRange(cursor, next)
            val decoded =
                try {
                    protocol.decode(packet)
                } catch (_: Exception) {
                    null
                }
            when (decoded) {
                is ExitPacket -> {
                    banchoSessions.revoke(token)
                    userService.updateLatestActivity(session.userId)
                    break
                }
                is PongPacket -> userService.updateLatestActivity(session.userId)
                else -> Unit
            }

            cursor = next
        }

        return ByteArray(0)
    }
}
