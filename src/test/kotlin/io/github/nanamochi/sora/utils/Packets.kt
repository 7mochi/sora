package io.github.nanamochi.sora.utils

import io.github.nanamochi.chio.packet.PacketType
import io.github.nanamochi.chio.protocol.Protocol
import kotlinx.io.Buffer
import kotlinx.io.readUShortLe
import kotlinx.io.write

fun parsePacketTypes(bytes: ByteArray, protocol: Protocol): List<PacketType> {
    val source = Buffer().apply { write(bytes) }
    val types = mutableListOf<PacketType>()

    while (!source.exhausted()) {
        val type = protocol.idMapping.packetType(source.readUShortLe().toInt())
        types += type
        protocol.format.read(source, type.maxSize)
    }

    return types
}
