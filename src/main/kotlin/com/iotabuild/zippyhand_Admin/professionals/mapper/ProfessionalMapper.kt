package com.iotabuild.zippyhand_Admin.professionals.mapper

import com.iotabuild.zippyhand_Admin.professionals.dto.ProfessionalResponse
import com.iotabuild.zippyhand_Admin.professionals.entity.ProfessionalEntity
import org.springframework.stereotype.Component

@Component
class ProfessionalMapper {

    fun toResponse(entity: ProfessionalEntity): ProfessionalResponse {
        return ProfessionalResponse(
            id = entity.id ?: 0,
            firebaseUid = entity.firebaseUid,
            email = entity.email,
            name = entity.name,
            photo = entity.photo,
            mobile = entity.mobile,
            address = entity.address,
            expertise = entity.expertise,
            aadhar = entity.aadhar,
            status = entity.status
        )
    }
}
