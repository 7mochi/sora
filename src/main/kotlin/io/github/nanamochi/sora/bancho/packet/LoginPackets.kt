package io.github.nanamochi.sora.bancho.packet

import io.github.nanamochi.chio.packet.model.UserInfo
import io.github.nanamochi.chio.packet.model.UserPresence
import io.github.nanamochi.chio.packet.model.UserStats
import io.github.nanamochi.chio.packet.model.UserStatus
import io.github.nanamochi.chio.packet.outbound.AccountRestrictedPacket
import io.github.nanamochi.chio.packet.outbound.AnnouncePacket
import io.github.nanamochi.chio.packet.outbound.ChannelInfoCompletePacket
import io.github.nanamochi.chio.packet.outbound.LoginPermissionsPacket
import io.github.nanamochi.chio.packet.outbound.LoginReplyPacket
import io.github.nanamochi.chio.packet.outbound.OutboundPacket
import io.github.nanamochi.chio.packet.outbound.ProtocolNegotiationPacket
import io.github.nanamochi.chio.packet.outbound.UserPresencePacket
import io.github.nanamochi.chio.packet.outbound.UserStatsPacket
import io.github.nanamochi.chio.packet.types.Country
import io.github.nanamochi.chio.packet.types.Mode
import io.github.nanamochi.chio.packet.types.Mods
import io.github.nanamochi.chio.packet.types.Permissions
import io.github.nanamochi.chio.packet.types.Status
import io.github.nanamochi.chio.protocol.Protocols
import io.github.nanamochi.sora.bancho.dto.request.LoginRequest
import io.github.nanamochi.sora.user.model.User

object LoginPackets {
    fun success(user: User, request: LoginRequest, welcome: String): ByteArray {
        val protocol = Protocols.latest
        val permissionBits = user.clientPrivileges
        val permissions =
            Permissions(
                if (user.isRestricted) permissionBits and Permissions.Regular.bits.inv()
                else permissionBits
            )
        val country = Country.fromAcronym(user.country.code.uppercase())
        val info =
            UserInfo(
                id = user.id,
                name = user.username,
                presence =
                    UserPresence(
                        false,
                        request.utcOffset,
                        country.ordinal,
                        permissions,
                        0.0,
                        0.0,
                        "",
                    ),
                status = UserStatus(Status.Idle, "", Mods.NoMod, Mode.Osu, "", -1, false),
                stats = UserStats(0, 0, 0, 100f, 0, 0),
            )

        val packets = mutableListOf<OutboundPacket>()
        packets += LoginReplyPacket(user.id)
        packets += ProtocolNegotiationPacket()
        packets += LoginPermissionsPacket(permissions)
        packets += AnnouncePacket(welcome)
        packets += ChannelInfoCompletePacket
        packets += UserPresencePacket(info)
        packets += UserStatsPacket(info)
        if (user.isRestricted) packets += AccountRestrictedPacket

        return protocol.encodeAll(packets)
    }

    fun failure(code: Int, message: String): ByteArray =
        Protocols.latest.encodeAll(listOf(AnnouncePacket(message), LoginReplyPacket(code)))
}
