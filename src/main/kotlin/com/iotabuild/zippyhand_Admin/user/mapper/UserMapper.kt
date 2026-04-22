package com.iotabuild.zippyhand_Admin.user.mapper

import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            firebaseUid = user.firebaseUid,
            name = user.name,
            email = user.email,
            phoneNumber = user.phoneNumber,
            role = user.role,
            status = user.status,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
