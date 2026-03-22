package com.iotabuild.zippyhand_Admin.user.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.user.dto.UserRegistrationRequest
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    fun registerUser(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: UserRegistrationRequest
    ): ApiResponse<UserResponse> {
        val userResponse = userService.registerOrUpdateUser(token, request)
        return ApiResponse.success(userResponse)
    }

    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<UserResponse?> {
        val userResponse = userService.findByFirebaseUid(token.uid)
        return ApiResponse.success(userResponse)
    }

    @GetMapping
    fun getAllUsers(): ApiResponse<List<UserResponse>> {
        return ApiResponse.success(userService.findAllUsers())
    }

    @PatchMapping("/{uid}/status")
    fun updateStatus(
        @PathVariable uid: String,
        @RequestParam status: com.iotabuild.zippyhand_Admin.user.entity.UserStatus
    ): ApiResponse<UserResponse> {
        return ApiResponse.success(userService.updateStatus(uid, status))
    }
}
