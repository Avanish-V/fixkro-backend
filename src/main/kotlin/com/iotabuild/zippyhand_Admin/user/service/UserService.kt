package com.iotabuild.zippyhand_Admin.user.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.user.dto.UserRegistrationRequest
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

@Service
class UserService(private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun registerOrUpdateUser(token: FirebaseToken, request: UserRegistrationRequest): UserResponse {
        val firebaseUid = token.uid
        val email = token.email ?: request.email
        
        val user = userRepository.findByFirebaseUid(firebaseUid)
            .map { existingUser ->
                logger.debug("Updating existing user: $firebaseUid")
                existingUser.name = request.name ?: existingUser.name
                existingUser.email = email ?: existingUser.email
                existingUser.phoneNumber = request.phoneNumber ?: existingUser.phoneNumber
                userRepository.save(existingUser)
            }
            .orElseGet {
                logger.info("Registering new user: $firebaseUid")
                userRepository.save(
                    User(
                        firebaseUid = firebaseUid,
                        name = request.name,
                        email = email,
                        phoneNumber = request.phoneNumber
                    )
                )
            }

        return mapToResponse(user)
    }

    fun findByFirebaseUid(uid: String): UserResponse? {
        return userRepository.findByFirebaseUid(uid).map { mapToResponse(it) }.orElse(null)
    }

    fun findAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional
    fun updateStatus(uid: String, status: com.iotabuild.zippyhand_Admin.user.entity.UserStatus): UserResponse {
        val user = userRepository.findByFirebaseUid(uid)
            .orElseThrow { RuntimeException("User not found: $uid") }
        user.status = status
        return mapToResponse(userRepository.save(user))
    }

    private fun mapToResponse(user: User): UserResponse {
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
