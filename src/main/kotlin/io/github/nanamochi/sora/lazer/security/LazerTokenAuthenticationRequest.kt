package io.github.nanamochi.sora.lazer.security

import io.quarkus.security.credential.TokenCredential
import io.quarkus.security.identity.request.TokenAuthenticationRequest

class LazerTokenAuthenticationRequest(token: TokenCredential) : TokenAuthenticationRequest(token)
