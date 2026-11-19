package io.github.nanamochi.sora.bancho.packet

import io.github.nanamochi.chio.packet.outbound.AnnouncePacket
import io.github.nanamochi.chio.packet.outbound.RestartPacket
import io.github.nanamochi.chio.protocol.Protocols

object SessionPackets {
    fun restart(message: String): ByteArray =
        Protocols.latest.encodeAll(listOf(AnnouncePacket(message), RestartPacket(0)))
}
