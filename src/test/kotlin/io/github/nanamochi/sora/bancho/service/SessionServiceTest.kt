package io.github.nanamochi.sora.bancho.service

import io.github.nanamochi.chio.packet.PacketType
import io.github.nanamochi.chio.protocol.Protocol
import io.github.nanamochi.chio.protocol.Protocols
import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.BANCHO_LOGIN_VERSION
import io.github.nanamochi.sora.utils.BANCHO_PASSWORD_MD5
import io.github.nanamochi.sora.utils.banchoLoginBody
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.parsePacketTypes
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.response.Response
import jakarta.inject.Inject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class SessionServiceTest {
    @Inject lateinit var users: UserService

    private val protocol = Protocols.latest

    @Test
    fun validTokenReturnsEmptyResponse() {
        val token = login(uniqueUsername("session_valid"))

        val response = session(token, ByteArray(0))

        assertEquals(0, response.asByteArray().size)
    }

    @Test
    fun logoutRevokesSession() {
        val token = login(uniqueUsername("session_logout"))

        session(token, exitPacket(protocol))

        val afterLogout = session(token, ByteArray(0))
        assertTrue(afterLogout.asByteArray().isNotEmpty())
        assertTrue(
            parsePacketTypes(afterLogout.asByteArray(), Protocols.latest)
                .contains(PacketType.BanchoRestart)
        )
    }

    @Test
    fun ignoresMalformedPacketLength() {
        val token = login(uniqueUsername("session_overflow"))
        val body =
            packetWithLength(protocol, protocol.idMapping.rawId(PacketType.OsuExit), Int.MAX_VALUE)

        val response = session(token, body)

        assertEquals(0, response.asByteArray().size)
    }

    @Test
    fun invalidTokenRequestsRestart() {
        val response = session("not-a-token", ByteArray(0))

        assertTrue(response.asByteArray().isNotEmpty())
        assertTrue(
            parsePacketTypes(response.asByteArray(), Protocols.latest)
                .contains(PacketType.BanchoRestart)
        )
    }

    @Test
    fun unknownPacketIsSkipped() {
        val token = login(uniqueUsername("session_unknown"))
        val body = unknownPacket(protocol) + exitPacket(protocol)

        val response = session(token, body)

        assertEquals(0, response.asByteArray().size)
        val afterLogout = session(token, ByteArray(0))
        assertTrue(
            parsePacketTypes(afterLogout.asByteArray(), Protocols.latest)
                .contains(PacketType.BanchoRestart)
        )
    }

    @Test
    fun handlesPongPacket() {
        val token = login(uniqueUsername("session_pong"))
        val body = packet(protocol, 4, ByteArray(0))

        val response = session(token, body)

        assertEquals(0, response.asByteArray().size)
    }

    private fun login(username: String, version: String = BANCHO_LOGIN_VERSION): String {
        createUser(users, username)
        val body = banchoLoginBody(username, BANCHO_PASSWORD_MD5, version)

        return given()
            .contentType("application/octet-stream")
            .body(body.toByteArray())
            .post("/")
            .then()
            .statusCode(200)
            .extract()
            .header("cho-token")
    }

    private fun session(token: String, body: ByteArray): Response =
        given()
            .contentType("application/octet-stream")
            .header("osu-token", token)
            .body(body)
            .post("/")
            .then()
            .statusCode(200)
            .extract()
            .response()

    private fun exitPacket(protocol: Protocol): ByteArray =
        packet(
            protocol,
            protocol.idMapping.rawId(PacketType.OsuExit),
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(),
        )

    private fun unknownPacket(protocol: Protocol): ByteArray = packet(protocol, 9999, ByteArray(0))

    private fun packetWithLength(protocol: Protocol, rawId: Int, length: Int): ByteArray {
        val header = ByteBuffer.allocate(protocol.format.headerSize).order(ByteOrder.LITTLE_ENDIAN)
        header.putShort(rawId.toShort())
        if (protocol.format.headerSize == 7) header.put(0)
        header.putInt(length)

        return header.array()
    }

    private fun packet(protocol: Protocol, rawId: Int, payload: ByteArray): ByteArray {
        val header = ByteBuffer.allocate(protocol.format.headerSize).order(ByteOrder.LITTLE_ENDIAN)
        header.putShort(rawId.toShort())
        if (protocol.format.headerSize == 7) header.put(0)
        header.putInt(payload.size)

        return header.array() + payload
    }
}
