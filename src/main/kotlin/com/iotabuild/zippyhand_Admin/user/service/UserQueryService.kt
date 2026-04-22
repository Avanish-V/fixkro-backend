package com.iotabuild.zippyhand_Admin.user.service

import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import com.iotabuild.zippyhand_Admin.user.mapper.UserMapper
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserQueryService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {

    fun findByFirebaseUid(uid: String): UserResponse? {
        return userRepository.findByFirebaseUid(uid).map { userMapper.toResponse(it) }.orElse(null)
    }

    fun findEntityByFirebaseUid(uid: String): User {
        return userRepository.findByFirebaseUid(uid)
            .orElseThrow { RuntimeException("User not found: $uid") }
    }

    fun findAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { userMapper.toResponse(it) }
    }
}
