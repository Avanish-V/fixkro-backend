package com.iotabuild.zippyhand_Admin.user.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.user.dto.UserRegistrationRequest
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.service.CustomerUserService
import com.iotabuild.zippyhand_Admin.user.service.UserQueryService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class CustomerUserController(
    private val customerUserService: CustomerUserService,
    private val queryService: UserQueryService
) {

    @PostMapping("/register")
    fun registerUser(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: UserRegistrationRequest
    ): ApiResponse<UserResponse> {
        return ApiResponse.success(customerUserService.registerOrUpdateUser(token, request))
    }

    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<UserResponse?> {
        return ApiResponse.success(queryService.findByFirebaseUid(token.uid))
    }
}
