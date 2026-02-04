package com.iotabuild.zippyhand_Admin.professionals.service

import com.iotabuild.zippyhand_Admin.professionals.models.*
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProfessionalService(
    private val professionalRepository: ProfessionalRepository
) {

    fun getAllProfessionals(): List<ProfessionalResponse> {
        return professionalRepository.findAll().map { entityToResponse(it) }
    }

    fun getProfessionalById(id: Long): ProfessionalResponse {
        val entity = professionalRepository.findById(id)
            .orElseThrow { NoSuchElementException("Professional not found with id: $id") }
        return entityToResponse(entity)
    }

    fun searchProfessionals(query: String): List<ProfessionalResponse> {
        return professionalRepository.searchProfessionals(query).map { entityToResponse(it) }
    }

    @Transactional
    fun createProfessional(request: CreateProfessionalRequest): ProfessionalResponse {
        val entity = ProfessionalEntity(
            name = request.name,
            photo = request.photo,
            mobile = request.mobile,
            address = request.address,
            expertise = request.expertise,
            aadharCard = request.aadharCard,
            idPhoto = request.idPhoto
        )
        val saved = professionalRepository.save(entity)
        return entityToResponse(saved)
    }

    @Transactional
    fun updateProfessional(id: Long, request: UpdateProfessionalRequest): ProfessionalResponse {
        val existing = professionalRepository.findById(id)
            .orElseThrow { NoSuchElementException("Professional not found with id: $id") }
        
        val updated = existing.copy(
            name = request.name,
            photo = request.photo,
            mobile = request.mobile,
            address = request.address,
            expertise = request.expertise,
            aadharCard = request.aadharCard,
            idPhoto = request.idPhoto,
            status = request.status,
            updatedAt = LocalDateTime.now()
        )
        val saved = professionalRepository.save(updated)
        return entityToResponse(saved)
    }

    @Transactional
    fun deleteProfessional(id: Long) {
        if (!professionalRepository.existsById(id)) {
            throw NoSuchElementException("Professional not found with id: $id")
        }
        professionalRepository.deleteById(id)
    }

    private fun entityToResponse(entity: ProfessionalEntity): ProfessionalResponse = ProfessionalResponse(
        id = entity.id ?: 0,
        name = entity.name,
        photo = entity.photo,
        mobile = entity.mobile,
        address = entity.address,
        expertise = entity.expertise,
        aadharCard = entity.aadharCard,
        idPhoto = entity.idPhoto,
        status = entity.status,
        completedJobs = entity.completedJobs,
        rating = entity.rating
    )
}
