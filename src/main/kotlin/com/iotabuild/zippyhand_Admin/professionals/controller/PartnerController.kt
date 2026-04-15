package com.iotabuild.zippyhand_Admin.professionals.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.professionals.models.CreateProfessionalRequest
import com.iotabuild.zippyhand_Admin.professionals.models.ProfessionalResponse
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/partners")
class PartnerController(private val professionalService: ProfessionalService) {

    @PostMapping("/register")
    fun registerPartner(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: CreateProfessionalRequest
    ): ApiResponse<ProfessionalResponse> {
        val response = professionalService.registerOrUpdateProfessional(token, request)
        return ApiResponse.success(response)
    }

    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<ProfessionalResponse?> {
        val response = professionalService.getProfessionalByUid(token.uid)
        return ApiResponse.success(response)
    }
}
