package com.iotabuild.zippyhand_Admin.user.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun registerOrUpdateUser(token: FirebaseToken): UserResponse {
        val firebaseUid = token.uid
        val phoneNumber = token.claims["phone_number"] as? String ?: ""

        val user = userRepository.findByFirebaseUid(firebaseUid)
            .map { existingUser ->
                existingUser.copy(
                    phoneNumber = phoneNumber,
                    updatedAt = LocalDateTime.now()
                ).let { userRepository.save(it) }
            }
            .orElseGet {
                userRepository.save(
                    User(
                        firebaseUid = firebaseUid,
                        phoneNumber = phoneNumber
                    )
                )
            }

        return UserResponse(
            id = user.id,
            firebaseUid = user.firebaseUid,
            phoneNumber = user.phoneNumber
        )
    }
}
