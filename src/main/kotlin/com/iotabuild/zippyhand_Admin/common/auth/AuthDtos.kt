package com.iotabuild.zippyhand_Admin.common.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val email: String
)
