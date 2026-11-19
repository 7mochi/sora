package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.lazer.dto.UserResponse
import io.github.nanamochi.sora.lazer.dto.toResponse
import io.github.nanamochi.sora.lazer.security.soraUser
import io.github.nanamochi.sora.shared.config.ServerConfig
import io.github.nanamochi.sora.shared.web.ApiError
import io.github.nanamochi.sora.user.model.Gamemode
import io.github.nanamochi.sora.user.repository.UserStatisticsRepository
import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status

@Path("/api/v2/me")
@Authenticated
class MeResource(
    private val identity: SecurityIdentity,
    private val statistics: UserStatisticsRepository,
    private val config: ServerConfig,
) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun me(): UserResponse {
        val user = identity.soraUser()
        val mode = Gamemode.fromValue(user.preferredMode) ?: Gamemode.OSU
        return user.toResponse(statistics.find(user.id, mode), config.publicUrl(), mode)
    }

    @GET
    @Path("/{mode}")
    @Produces(MediaType.APPLICATION_JSON)
    fun meMode(@PathParam("mode") mode: String): UserResponse {
        val user = identity.soraUser()
        val gamemode = Gamemode.fromAlias(mode) ?: throw unknownMode()
        return user.toResponse(statistics.find(user.id, gamemode), config.publicUrl(), gamemode)
    }

    private fun unknownMode(): WebApplicationException =
        WebApplicationException(
            Response.status(Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiError("invalid_request", "Unknown mode"))
                .build()
        )
}
