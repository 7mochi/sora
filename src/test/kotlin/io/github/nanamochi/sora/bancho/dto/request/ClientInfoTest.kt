package io.github.nanamochi.sora.bancho.dto.request

import jakarta.ws.rs.core.HttpHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ClientInfoTest {
    @Test
    fun usesRemoteAddressInsteadOfForwardedFor() {
        val clientInfo = ClientInfo.fromRequest(headers(), "198.51.100.7")

        assertEquals("198.51.100.7", clientInfo.ipAddress)
        assertEquals("test", clientInfo.userAgent)
    }

    @Test
    fun truncatesLongIpAndUserAgent() {
        val headers = headers()
        `when`(headers.getHeaderString("User-Agent")).thenReturn("a".repeat(400))

        val clientInfo = ClientInfo.fromRequest(headers, "1".repeat(60))

        assertEquals(45, clientInfo.ipAddress.length)
        assertEquals(255, clientInfo.userAgent?.length)
    }

    @Test
    fun fallsBackToLoopbackWhenRemoteAddressIsMissing() {
        val clientInfo = ClientInfo.fromRequest(headers(), null)

        assertEquals("127.0.0.1", clientInfo.ipAddress)
    }

    @Test
    fun fallsBackToLoopbackWhenRemoteAddressIsBlank() {
        val clientInfo = ClientInfo.fromRequest(headers(), "   ")

        assertEquals("127.0.0.1", clientInfo.ipAddress)
    }

    @Test
    fun leavesUserAgentNullWhenHeaderIsMissing() {
        val clientInfo = ClientInfo.fromRequest(headers(null), "1.2.3.4")

        assertNull(clientInfo.userAgent)
    }

    private fun headers(userAgent: String? = "test"): HttpHeaders {
        val headers = mock(HttpHeaders::class.java)
        `when`(headers.getHeaderString("X-Forwarded-For")).thenReturn("203.0.113.99")
        `when`(headers.getHeaderString("User-Agent")).thenReturn(userAgent)
        return headers
    }
}
