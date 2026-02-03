package com.iotabuild.zippyhand_Admin.user.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    fun registerUser(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<UserResponse> {
        val userResponse = userService.registerOrUpdateUser(token)
        return ApiResponse.success(userResponse)
    }
}
