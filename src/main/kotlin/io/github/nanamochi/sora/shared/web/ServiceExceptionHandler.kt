package io.github.nanamochi.sora.shared.web

import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ServiceExceptionHandler : ExceptionMapper<ServiceException> {
    override fun toResponse(exception: ServiceException): Response {
        val code = exception.errorCode
        val error =
            if (code == ErrorCode.USER_NOT_FOUND) {
                ApiError("User not found")
            } else {
                ApiError(code.name.lowercase(), exception.message)
            }

        return Response.status(code.status).type(MediaType.APPLICATION_JSON).entity(error).build()
    }
}
