package io.github.nanamochi.sora.shared.web

import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceExceptionHandlerTest {
    @Test
    fun mapsServiceErrorsThatAreNotUserNotFound() {
        val response =
            ServiceExceptionHandler().toResponse(ServiceException(ErrorCode.AUTH_INVALID_GRANT))

        assertEquals(400, response.status)
    }
}
