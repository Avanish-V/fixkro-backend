package com.iotabuild.zippyhand_Admin.user.service

import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.UserStatus
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val queryService: UserQueryService
) {

    @Transactional
    fun updateStatus(uid: String, status: UserStatus): UserResponse {
        val user = queryService.findEntityByFirebaseUid(uid)
        user.status = status
        return queryService.findByFirebaseUid(userRepository.save(user).firebaseUid)!!
    }
}
