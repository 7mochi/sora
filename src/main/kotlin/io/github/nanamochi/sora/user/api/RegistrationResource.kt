package io.github.nanamochi.sora.user.api

import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.user.dto.response.RegistrationErrors
import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.service.UserService
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.FormParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/users")
class RegistrationResource(private val users: UserService) {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    fun register(
        @FormParam("user[username]") username: String?,
        @FormParam("user[user_email]") email: String?,
        @FormParam("user[password]") password: String?,
        @FormParam("check") check: Int?,
    ): Response {
        if (
            username.isNullOrBlank() ||
                email.isNullOrBlank() ||
                password.isNullOrBlank() ||
                check == null
        ) {
            return Response.status(400)
                .type(MediaType.TEXT_PLAIN)
                .entity("Missing required params")
                .build()
        }

        val errors = users.validateRegistration(username, email, password)
        if (!errors.isEmpty) return formError(errors)

        if (check != 1) {
            return try {
                users.create(username, email, password, CountryCode.XX)
                Response.ok("ok").type(MediaType.TEXT_PLAIN).build()
            } catch (error: ServiceException) {
                formError(registrationError(error))
            }
        }

        return Response.ok("ok").type(MediaType.TEXT_PLAIN).build()
    }

    private fun registrationError(error: ServiceException): RegistrationErrors =
        RegistrationErrors(username = listOf(error.message))

    private fun formError(errors: RegistrationErrors): Response {
        val fields = mutableMapOf<String, List<String>>()
        if (errors.username.isNotEmpty()) fields["username"] = errors.username
        if (errors.email.isNotEmpty()) fields["user_email"] = errors.email
        if (errors.password.isNotEmpty()) fields["password"] = errors.password

        return Response.status(400)
            .type(MediaType.APPLICATION_JSON)
            .entity(mapOf("form_error" to mapOf("user" to fields)))
            .build()
    }
}
