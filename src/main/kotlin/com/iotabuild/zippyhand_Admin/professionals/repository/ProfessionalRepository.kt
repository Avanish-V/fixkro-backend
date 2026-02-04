package com.iotabuild.zippyhand_Admin.professionals.repository

import com.iotabuild.zippyhand_Admin.professionals.models.ProfessionalEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProfessionalRepository : JpaRepository<ProfessionalEntity, Long> {
    fun findByMobile(mobile: String): ProfessionalEntity?
    
    @Query("SELECT p FROM ProfessionalEntity p WHERE p.mobile LIKE %:query% OR p.name LIKE %:query%")
    fun searchProfessionals(query: String): List<ProfessionalEntity>
}
