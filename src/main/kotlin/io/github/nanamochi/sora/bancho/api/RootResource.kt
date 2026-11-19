package io.github.nanamochi.sora.bancho.api

import io.github.nanamochi.sora.bancho.dto.request.ClientInfo
import io.github.nanamochi.sora.bancho.packet.SessionPackets
import io.github.nanamochi.sora.bancho.service.LoginService
import io.github.nanamochi.sora.bancho.service.SessionService
import io.github.nanamochi.sora.shared.error.ServiceException
import io.vertx.ext.web.RoutingContext
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/")
class RootResource(
    private val loginService: LoginService,
    private val sessionService: SessionService,
) {
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    fun handle(
        @HeaderParam("osu-token") token: String?,
        @Context headers: HttpHeaders,
        @Context routingContext: RoutingContext,
        body: ByteArray,
    ): Response {
        if (token != null) {
            val bytes =
                try {
                    sessionService.processPackets(token, body)
                } catch (_: ServiceException) {
                    SessionPackets.restart("Server has restarted.")
                }

            return Response.ok(bytes).type(MediaType.APPLICATION_OCTET_STREAM).build()
        }

        val clientInfo =
            ClientInfo.fromRequest(headers, routingContext.request().remoteAddress()?.host())

        return loginService.login(body, clientInfo)
    }
}
