package com.iotabuild.zippyhand_Admin.professionals.service

import com.iotabuild.zippyhand_Admin.professionals.dto.CreateProfessionalRequest
import com.iotabuild.zippyhand_Admin.professionals.dto.ProfessionalResponse
import com.iotabuild.zippyhand_Admin.professionals.dto.UpdateProfessionalRequest
import com.iotabuild.zippyhand_Admin.professionals.entity.ProfessionalEntity
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminProfessionalService(
    private val professionalRepository: ProfessionalRepository,
    private val queryService: ProfessionalQueryService
) {

    @Transactional
    fun createProfessional(request: CreateProfessionalRequest): ProfessionalResponse {
        val entity = ProfessionalEntity(
            firebaseUid = request.firebaseUid ?: "",
            email = request.email ?: "",
            name = request.name,
            photo = request.photo ?: "",
            mobile = request.mobile ?: "",
            address = request.address,
            expertise = request.expertise,
            aadhar = request.aadhar,
            status = "ACTIVE"
        )
        val saved = professionalRepository.save(entity)
        return queryService.getProfessionalById(saved.id!!)
    }

    @Transactional
    fun updateProfessional(id: Long, request: UpdateProfessionalRequest): ProfessionalResponse {
        val existing = professionalRepository.findById(id)
            .orElseThrow { NoSuchElementException("Professional not found with id: $id") }
        
        val updated = existing.copy(
            firebaseUid = request.firebaseUid ?: existing.firebaseUid,
            email = request.email ?: existing.email,
            name = request.name ?: existing.name,
            photo = request.photo ?: existing.photo,
            mobile = request.mobile ?: existing.mobile,
            address = request.address ?: existing.address,
            expertise = request.expertise ?: existing.expertise,
            aadhar = request.aadhar ?: existing.aadhar,
            status = request.status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
        val saved = professionalRepository.save(updated)
        return queryService.getProfessionalById(saved.id!!)
    }

    @Transactional
    fun deleteProfessional(id: Long) {
        if (!professionalRepository.existsById(id)) {
            throw NoSuchElementException("Professional not found with id: $id")
        }
        professionalRepository.deleteById(id)
    }
}
