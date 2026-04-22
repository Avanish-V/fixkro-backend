package com.iotabuild.zippyhand_Admin.professionals.service

import com.iotabuild.zippyhand_Admin.professionals.dto.ProfessionalResponse
import com.iotabuild.zippyhand_Admin.professionals.mapper.ProfessionalMapper
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.springframework.stereotype.Service

@Service
class ProfessionalQueryService(
    private val professionalRepository: ProfessionalRepository,
    private val professionalMapper: ProfessionalMapper
) {

    fun getAllProfessionals(): List<ProfessionalResponse> {
        return professionalRepository.findAll().map { professionalMapper.toResponse(it) }
    }

    fun getProfessionalById(id: Long): ProfessionalResponse {
        val entity = professionalRepository.findById(id)
            .orElseThrow { NoSuchElementException("Professional not found with id: $id") }
        return professionalMapper.toResponse(entity)
    }

    fun findByUid(uid: String): ProfessionalResponse? {
        val entity = professionalRepository.findByFirebaseUid(uid) ?: return null
        return professionalMapper.toResponse(entity)
    }

    fun searchProfessionals(query: String): List<ProfessionalResponse> {
        return professionalRepository.searchProfessionals(query).map { professionalMapper.toResponse(it) }
    }
}
