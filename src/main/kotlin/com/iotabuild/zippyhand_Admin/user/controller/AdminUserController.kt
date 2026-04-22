package com.iotabuild.zippyhand_Admin.user.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.UserStatus
import com.iotabuild.zippyhand_Admin.user.service.AdminUserService
import com.iotabuild.zippyhand_Admin.user.service.UserQueryService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val adminUserService: AdminUserService,
    private val queryService: UserQueryService
) {

    @GetMapping
    fun getAllUsers(): ApiResponse<List<UserResponse>> {
        return ApiResponse.success(queryService.findAllUsers())
    }

    @PatchMapping("/{uid}/status")
    fun updateStatus(
        @PathVariable uid: String,
        @RequestParam status: UserStatus
    ): ApiResponse<UserResponse> {
        return ApiResponse.success(adminUserService.updateStatus(uid, status))
    }
}
