package io.github.nanamochi.sora.user.api

import io.github.nanamochi.sora.shared.error.ErrorCode
import io.github.nanamochi.sora.shared.error.ServiceException
import io.github.nanamochi.sora.user.dto.response.RegistrationErrors
import io.github.nanamochi.sora.user.model.CountryCode
import io.github.nanamochi.sora.user.model.GroupIdentifier
import io.github.nanamochi.sora.user.service.UserService
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`

@QuarkusTest
class RegistrationResourceErrorTest {
    @InjectMock lateinit var users: UserService

    @Test
    fun rejectsUserCreatedAfterValidation() {
        `when`(users.validateRegistration("race_user", "race@example.com", "secret123"))
            .thenReturn(RegistrationErrors())
        doThrow(ServiceException(ErrorCode.USER_ALREADY_EXISTS))
            .`when`(users)
            .create(
                "race_user",
                "race@example.com",
                "secret123",
                CountryCode.XX,
                GroupIdentifier.DEFAULT,
                osuSubscriber = false,
                restricted = false,
            )

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", "race_user")
            .formParam("user[user_email]", "race@example.com")
            .formParam("user[password]", "secret123")
            .formParam("check", "0")
            .post("/users")
            .then()
            .statusCode(400)
            .body("form_error.user.username", hasItem(containsString("already exists")))
    }
}
