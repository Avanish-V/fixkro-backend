package com.iotabuild.zippyhand_Admin.user.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.user.dto.UserRegistrationRequest
import com.iotabuild.zippyhand_Admin.user.dto.UserResponse
import com.iotabuild.zippyhand_Admin.user.entity.User
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerUserService(
    private val userRepository: UserRepository,
    private val queryService: UserQueryService
) {
    private val logger = LoggerFactory.getLogger(CustomerUserService::class.java)

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

        return queryService.findByFirebaseUid(user.firebaseUid)!!
    }
}
