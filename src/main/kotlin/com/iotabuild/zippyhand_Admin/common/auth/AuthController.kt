package com.iotabuild.zippyhand_Admin.common.auth

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ApiResponse<LoginResponse> {
        return try {
            ApiResponse.success(authService.login(request))
        } catch (e: Exception) {
            ApiResponse.error(e.message ?: "Authentication failed")
        }
    }
}
