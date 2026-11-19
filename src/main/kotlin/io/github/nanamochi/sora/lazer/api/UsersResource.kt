package io.github.nanamochi.sora.lazer.api

import io.github.nanamochi.sora.lazer.dto.UserResponse
import io.github.nanamochi.sora.lazer.dto.toResponse
import io.github.nanamochi.sora.shared.config.ServerConfig
import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.shared.web.ApiError
import io.github.nanamochi.sora.user.model.Gamemode
import io.github.nanamochi.sora.user.model.User
import io.github.nanamochi.sora.user.repository.UserStatisticsRepository
import io.github.nanamochi.sora.user.service.UserService
import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status

@Path("/api/v2/users")
@Authenticated
class UsersResource(
    private val users: UserService,
    private val statistics: UserStatisticsRepository,
    private val config: ServerConfig,
) {
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun user(@PathParam("id") id: String): UserResponse {
        val user = resolve(id)
        return user.toResponse(
            statistics.find(user.id, Gamemode.OSU),
            config.publicUrl(),
            Gamemode.OSU,
        )
    }

    @GET
    @Path("/{id}/{mode}")
    @Produces(MediaType.APPLICATION_JSON)
    fun userMode(@PathParam("id") id: String, @PathParam("mode") mode: String): UserResponse {
        val gamemode = Gamemode.fromAlias(mode) ?: throw unknownMode()
        val user = resolve(id)
        return user.toResponse(statistics.find(user.id, gamemode), config.publicUrl(), gamemode)
    }

    private fun resolve(id: String): User {
        val user =
            if (id.startsWith("@")) {
                users.findByUsernameOrEmail(id)
            } else {
                id.toIntOrNull()?.let(users::findById)
            }

        return user ?: throw ServiceException(ErrorCode.USER_NOT_FOUND)
    }

    private fun unknownMode(): WebApplicationException =
        WebApplicationException(
            Response.status(Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiError("invalid_request", "Unknown mode"))
                .build()
        )
}
