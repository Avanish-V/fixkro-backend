package com.iotabuild.zippyhand_Admin.professionals.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.professionals.dto.CreateProfessionalRequest
import com.iotabuild.zippyhand_Admin.professionals.dto.ProfessionalResponse
import com.iotabuild.zippyhand_Admin.professionals.entity.ProfessionalEntity
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MobileProfessionalService(
    private val professionalRepository: ProfessionalRepository,
    private val queryService: ProfessionalQueryService
) {
    private val logger = LoggerFactory.getLogger(MobileProfessionalService::class.java)

    @Transactional
    fun registerOrUpdate(token: FirebaseToken, request: CreateProfessionalRequest): ProfessionalResponse {
        val firebaseUid = token.uid
        val existing = professionalRepository.findByFirebaseUid(firebaseUid)

        validateProfileRequest(request)

        val entity = if (existing != null) {
            val newStatus = when (existing.status) {
                "INCOMPLETE" -> "PENDING"
                "ACTIVE" -> "ACTIVE"
                else -> existing.status
            }

            existing.copy(
                name = request.name,
                photo = request.photo ?: existing.photo,
                mobile = request.mobile ?: existing.mobile,
                address = request.address,
                expertise = if (request.expertise.isNotEmpty()) request.expertise else existing.expertise,
                status = newStatus,
                updatedAt = LocalDateTime.now()
            )
        } else {
            ProfessionalEntity(
                firebaseUid = firebaseUid,
                email = token.email ?: request.email ?: "",
                name = request.name,
                photo = request.photo ?: "",
                mobile = request.mobile ?: "",
                address = request.address,
                expertise = request.expertise,
                status = "PENDING",
                updatedAt = LocalDateTime.now()
            )
        }

        val saved = professionalRepository.save(entity)
        return queryService.getProfessionalById(saved.id!!)
    }

    @Transactional
    fun submitForReview(uid: String): ProfessionalResponse {
        val entity = professionalRepository.findByFirebaseUid(uid)
            ?: throw RuntimeException("Professional profile not found")
            
        entity.status = "PENDING"
        entity.updatedAt = LocalDateTime.now()
        val saved = professionalRepository.save(entity)
        return queryService.getProfessionalById(saved.id!!)
    }

    @Transactional
    fun checkAndRegister(uid: String, email: String, name: String?): ProfessionalEntity {
        val existing = professionalRepository.findByFirebaseUid(uid)
        return if (existing != null) {
            existing
        } else {
            val newProfessional = ProfessionalEntity(
                firebaseUid = uid,
                email = email,
                name = name ?: "Professional",
                status = "INCOMPLETE"
            )
            professionalRepository.save(newProfessional)
        }
    }

    private fun validateProfileRequest(request: CreateProfessionalRequest) {
        if (request.name.isBlank()) throw IllegalArgumentException("Name is mandatory")
        if (request.mobile.isNullOrBlank()) throw IllegalArgumentException("Mobile number is mandatory")
        if (request.address.isBlank()) throw IllegalArgumentException("Address is mandatory")
        if (request.photo.isNullOrBlank()) throw IllegalArgumentException("Profile photo is mandatory")
        if (request.expertise.isEmpty()) throw IllegalArgumentException("At least one expertise must be selected")
    }
}
