package io.github.nanamochi.sora.utils

const val BANCHO_PASSWORD_MD5 = "5d7845ac6ee7cfffafc5fe5f35cf666d" // secret123
const val BANCHO_LOGIN_VERSION = "b20990101"

fun banchoLoginBody(
    username: String,
    passwordMd5: String = BANCHO_PASSWORD_MD5,
    version: String = BANCHO_LOGIN_VERSION,
): String = "$username\n$passwordMd5\n$version|0|0|a:b:c:d:e|0\n"
