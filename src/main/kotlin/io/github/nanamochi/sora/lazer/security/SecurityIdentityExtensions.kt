package io.github.nanamochi.sora.lazer.security

import io.github.nanamochi.sora.user.model.User
import io.quarkus.security.identity.SecurityIdentity

fun SecurityIdentity.soraUser(): User = getAttribute("sora.user")

fun SecurityIdentity.soraToken(): String = getAttribute("sora.token")
