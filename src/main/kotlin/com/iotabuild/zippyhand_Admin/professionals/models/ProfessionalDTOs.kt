package com.iotabuild.zippyhand_Admin.professionals.models

data class ProfessionalResponse(
    val id: Long,
    val firebaseUid: String,
    val name: String,
    val photo: String,
    val mobile: String,
    val address: String,
    val expertise: List<String>,
    val aadharCard: String,
    val idPhoto: String,
    val status: String,
)

data class CreateProfessionalRequest(
    val firebaseUid: String = "",
    val name: String,
    val photo: String = "",
    val mobile: String,
    val address: String = "",
    val expertise: List<String> = emptyList(),
    val aadharCard: String = "",
    val idPhoto: String = ""
)

data class UpdateProfessionalRequest(
    val firebaseUid: String,
    val name: String,
    val photo: String,
    val mobile: String,
    val address: String,
    val expertise: List<String>,
    val aadharCard: String,
    val idPhoto: String,
    val status: String
)
