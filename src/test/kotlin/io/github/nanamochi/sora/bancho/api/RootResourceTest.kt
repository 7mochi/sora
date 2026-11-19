package io.github.nanamochi.sora.bancho.api

import io.github.nanamochi.chio.packet.PacketType
import io.github.nanamochi.chio.protocol.Protocol
import io.github.nanamochi.chio.protocol.Protocols
import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.repository.BanchoSessionRepository
import io.github.nanamochi.sora.bancho.service.BanchoSessionService
import io.github.nanamochi.sora.shared.security.PasswordHasher
import io.github.nanamochi.sora.user.repository.UserRepository
import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.BANCHO_LOGIN_VERSION
import io.github.nanamochi.sora.utils.BANCHO_PASSWORD_MD5
import io.github.nanamochi.sora.utils.banchoLoginBody
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.parsePacketTypes
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.response.Response
import jakarta.inject.Inject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import kotlinx.io.Buffer
import kotlinx.io.readUShortLe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@QuarkusTest
class RootResourceTest {
    @Inject lateinit var users: UserService
    @Inject lateinit var hasher: PasswordHasher
    @Inject lateinit var banchoSessions: BanchoSessionService
    @Inject lateinit var sessions: BanchoSessionRepository
    @Inject lateinit var userRepository: UserRepository

    @Test
    fun loginCreatesSessionAndReturnsLoginPackets() {
        val username = uniqueUsername("bancho_player")
        val user = createUser(users, username)

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)
        val token = response.header("cho-token")
        val protocol = Protocols.latest
        val types = parsePacketTypes(response.asByteArray(), protocol)

        assertNotEquals("no", token)
        val session = requireNotNull(sessions.findByToken(token))
        assertEquals(user.id, session.userId)
        assertEquals(token, session.accessToken)
        assertEquals(user.id, loginReply(response.asByteArray(), protocol))
        assertTrue(types.contains(PacketType.BanchoLoginReply))
        assertTrue(types.contains(PacketType.BanchoLoginPermissions))
        assertTrue(types.contains(PacketType.BanchoUserPresence))
        assertTrue(types.contains(PacketType.BanchoProtocolNegotiation))
        assertTrue(types.contains(PacketType.BanchoAnnounce))
        assertTrue(types.contains(PacketType.BanchoChannelInfoComplete))
        assertTrue(types.contains(PacketType.BanchoUserStats))
    }

    @Test
    fun rejectsMalformedLoginBody() {
        val response =
            given()
                .contentType("application/octet-stream")
                .body("malformed".toByteArray())
                .post("/")
                .then()
                .statusCode(200)
                .extract()
                .response()

        assertEquals("no", response.header("cho-token"))
        assertEquals(-1, loginReply(response.asByteArray(), Protocols.latest))
    }

    @Test
    fun loginUpdatesLatestActivity() {
        val username = uniqueUsername("bancho_activity")
        val user = createUser(users, username)
        QuarkusTransaction.requiringNew().run {
            userRepository.updateLatestActivity(user.id, Instant.EPOCH)
        }

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)

        assertNotEquals("no", response.header("cho-token"))
        assertEquals(
            1L,
            userRepository.count("id = ?1 and latestActivity > ?2", user.id, Instant.EPOCH),
        )
    }

    @Test
    fun rejectsLoginWhenSessionLimitReached() {
        val username = uniqueUsername("bancho_limit")
        val user = createUser(users, username)
        repeat(20) { banchoSessions.create(user, ClientInfo("127.0.0.1", "test")) }

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)

        assertEquals("no", response.header("cho-token"))
        assertEquals(-2, loginReply(response.asByteArray(), Protocols.latest))
        assertEquals(20L, sessions.countActiveByUserId(user.id, Instant.now()))
    }

    @Test
    fun oldClientCannotLogin() {
        val username = uniqueUsername("bancho_old")
        val user = createUser(users, username)

        val response = login(username, BANCHO_PASSWORD_MD5, "b340")

        assertEquals("no", response.header("cho-token"))
        assertEquals(-2, loginReply(response.asByteArray(), Protocols.latest))
        assertEquals(0L, sessions.countActiveByUserId(user.id, Instant.now()))
    }

    @Test
    fun nonDonorIsNotSupporter() {
        val username = uniqueUsername("bancho_non_donor")
        createUser(users, username)

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)
        val protocol = Protocols.latest

        assertEquals(1, permissionsBits(response.asByteArray(), protocol))
    }

    @Test
    fun donorIsSupporter() {
        val username = uniqueUsername("bancho_donor")
        createUser(users, username, osuSubscriber = true)

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)
        val protocol = Protocols.latest

        assertEquals(5, permissionsBits(response.asByteArray(), protocol))
    }

    @Test
    fun rejectsWrongPasswordWithoutSession() {
        val username = uniqueUsername("bancho_wrong")
        val user = createUser(users, username)

        val response = login(username, hasher.md5Hex("wrong"), BANCHO_LOGIN_VERSION)

        assertEquals("no", response.header("cho-token"))
        assertEquals(-1, loginReply(response.asByteArray(), Protocols.latest))
        assertEquals(0L, sessions.countActiveByUserId(user.id, Instant.now()))
    }

    @Test
    fun restrictedUserLogsInWithoutRegularPermission() {
        val username = uniqueUsername("bancho_restricted")
        val user = createUser(users, username, restricted = true)

        val response = login(username, BANCHO_PASSWORD_MD5, BANCHO_LOGIN_VERSION)
        val protocol = Protocols.latest
        val types = parsePacketTypes(response.asByteArray(), protocol)

        assertNotEquals("no", response.header("cho-token"))
        assertEquals(user.id, loginReply(response.asByteArray(), protocol))
        assertEquals(0, permissionsBits(response.asByteArray(), protocol))
        assertTrue(types.contains(PacketType.BanchoAccountRestricted))
    }

    private fun login(username: String, passwordMd5: String, version: String): Response =
        given()
            .contentType("application/octet-stream")
            .body(banchoLoginBody(username, passwordMd5, version).toByteArray())
            .post("/")
            .then()
            .statusCode(200)
            .extract()
            .response()

    private fun readIntFromPacket(bytes: ByteArray, protocol: Protocol, type: PacketType): Int {
        val source = Buffer().apply { write(bytes) }

        while (!source.exhausted()) {
            val actual = protocol.idMapping.packetType(source.readUShortLe().toInt())
            val payload = protocol.format.read(source, actual.maxSize)
            if (actual == type) {
                return ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN).int
            }
        }

        error("$type not found")
    }

    private fun permissionsBits(bytes: ByteArray, protocol: Protocol): Int {
        // feature bits under test
        return readIntFromPacket(bytes, protocol, PacketType.BanchoLoginPermissions)
    }

    private fun loginReply(bytes: ByteArray, protocol: Protocol): Int {
        // user id or error code under test
        return readIntFromPacket(bytes, protocol, PacketType.BanchoLoginReply)
    }
}
