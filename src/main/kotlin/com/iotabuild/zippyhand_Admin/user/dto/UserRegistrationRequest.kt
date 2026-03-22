package com.iotabuild.zippyhand_Admin.user.dto

import com.iotabuild.zippyhand_Admin.user.entity.UserRole
import com.iotabuild.zippyhand_Admin.user.entity.UserStatus
import java.time.LocalDateTime

data class UserRegistrationRequest(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)

data class UserResponse(
    val id: Long?,
    val firebaseUid: String,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
