package com.iotabuild.zippyhand_Admin.user.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request body for user registration.
 * Note: UID and Phone Number are extracted from the Firebase ID Token for security.
 * This DTO can be used for additional profile data like 'displayName' or 'email'.
 */
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
    val phoneNumber: String? = null
)
