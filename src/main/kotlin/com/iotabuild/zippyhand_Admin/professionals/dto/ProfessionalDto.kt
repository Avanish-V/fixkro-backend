package com.iotabuild.zippyhand_Admin.professionals.dto

data class ProfessionalResponse(
    val id: Long,
    val firebaseUid: String,
    val email: String,
    val name: String,
    val photo: String,
    val mobile: String,
    val address: String,
    val expertise: List<String>,
    val aadhar: String,
    val status: String,
)

data class CreateProfessionalRequest(
    val firebaseUid: String? = null,
    val email: String? = null,
    val name: String,
    val photo: String? = null,
    val mobile: String? = null,
    val address: String = "",
    val expertise: List<String> = emptyList(),
    val aadhar: String = ""
)

data class UpdateProfessionalRequest(
    val firebaseUid: String? = null,
    val email: String? = null,
    val name: String? = null,
    val photo: String? = null,
    val mobile: String? = null,
    val address: String? = null,
    val expertise: List<String>? = null,
    val aadhar: String? = null,
    val status: String? = null
)

data class AuthRequest(
    val idToken: String
)

data class AuthResponse(
    val uid: String,
    val status: String,
    val isNewUser: Boolean
)
