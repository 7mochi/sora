package io.github.nanamochi.sora.user.api

import io.github.nanamochi.sora.user.service.UserService
import io.github.nanamochi.sora.utils.createUser
import io.github.nanamochi.sora.utils.uniqueUsername
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@QuarkusTest
class RegistrationResourceTest {
    @Inject lateinit var users: UserService

    @Test
    fun checkOnlyDoesNotCreateAccount() {
        val username = uniqueUsername("check")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "$username@example.com")
            .formParam("user[password]", "secret123")
            .formParam("check", "1")
            .post("/users")
            .then()
            .statusCode(200)
            .body(equalTo("ok"))

        assertNull(users.findByUsernameOrEmail(username))
    }

    @Test
    fun createsAccount() {
        val username = uniqueUsername("bancho")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "$username@example.com")
            .formParam("user[password]", "secret123")
            .formParam("check", "0")
            .post("/users")
            .then()
            .statusCode(200)
            .body(equalTo("ok"))

        assertNotNull(users.findByUsernameOrEmail(username))
    }

    @Test
    fun rejectsDuplicateUsername() {
        val username = uniqueUsername("duplicate")
        createUser(users, username)

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "other_$username@example.com")
            .formParam("user[password]", "secret123")
            .formParam("check", "0")
            .post("/users")
            .then()
            .statusCode(400)
            .body("form_error.user.username", hasItem(containsString("already taken")))
    }

    @Test
    fun rejectsMissingCheck() {
        val username = uniqueUsername("nocheck")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "$username@example.com")
            .formParam("user[password]", "secret123")
            .post("/users")
            .then()
            .statusCode(400)
            .body(equalTo("Missing required params"))

        assertNull(users.findByUsernameOrEmail(username))
    }

    @Test
    fun rejectsShortPassword() {
        val username = uniqueUsername("shortpw")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "$username@example.com")
            .formParam("user[password]", "short")
            .formParam("check", "0")
            .post("/users")
            .then()
            .statusCode(400)
            .body("form_error.user.password", hasItem(containsString("8-32")))
    }

    @Test
    fun rejectsInvalidEmail() {
        val username = uniqueUsername("bad_email")

        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("user[username]", username)
            .formParam("user[user_email]", "not-an-email")
            .formParam("user[password]", "secret123")
            .formParam("check", "0")
            .post("/users")
            .then()
            .statusCode(400)
            .body("form_error.user.user_email", hasItem(containsString("Invalid email")))
    }
}
