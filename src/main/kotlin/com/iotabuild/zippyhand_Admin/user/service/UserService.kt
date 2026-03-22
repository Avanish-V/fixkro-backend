package com.iotabuild.zippyhand_Admin.user.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.user.dto.UserRegistrationRequest
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun registerOrUpdateUser(token: FirebaseToken, request: UserRegistrationRequest): UserResponse {
        val firebaseUid = token.uid
        val email = token.email ?: request.email
        
        val user = userRepository.findByFirebaseUid(firebaseUid)
            .map { existingUser ->
                existingUser.let {
                    it.updatedAt = LocalDateTime.now()
                    // We don't necessarily want to override name/email/phone from token if they are null in request
                    userRepository.save(it.copy(
                        name = request.name ?: it.name,
                        email = email ?: it.email,
                        phoneNumber = request.phoneNumber ?: it.phoneNumber
                    ))
                }
            }
            .orElseGet {
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

    private fun mapToResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            firebaseUid = user.firebaseUid,
            name = user.name,
            email = user.email,
            phoneNumber = user.phoneNumber
        )
    }
}
