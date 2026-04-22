package com.iotabuild.zippyhand_Admin.professionals.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.common.service.S3StorageService
import com.iotabuild.zippyhand_Admin.professionals.dto.*
import com.iotabuild.zippyhand_Admin.professionals.service.MobileProfessionalService
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalQueryService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * Unified controller for all Partner (Technician) mobile operations.
 */
@RestController
@RequestMapping("/api/v1/partners")
class PartnerController(
    private val mobileProfessionalService: MobileProfessionalService,
    private val queryService: ProfessionalQueryService,
    private val s3Service: S3StorageService
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<ProfessionalResponse?> {
        val response = queryService.findByUid(token.uid)
        return ApiResponse.success(response)
    }

    @PostMapping("/register")
    fun registerPartner(
        @AuthenticationPrincipal token: FirebaseToken,
        @Valid @RequestBody request: CreateProfessionalRequest
    ): ApiResponse<ProfessionalResponse> {
        val response = mobileProfessionalService.registerOrUpdate(token, request)
        return ApiResponse.success(response)
    }

    @PostMapping("/me/update")
    fun updateMyProfile(
        @AuthenticationPrincipal token: FirebaseToken,
        @Valid @RequestBody request: CreateProfessionalRequest
    ): ApiResponse<ProfessionalResponse> {
        val response = mobileProfessionalService.registerOrUpdate(token, request)
        return ApiResponse.success(response)
    }

    @PostMapping("/submit-review")
    fun submitProfileForReview(@AuthenticationPrincipal token: FirebaseToken): ApiResponse<ProfessionalResponse> {
        val response = mobileProfessionalService.submitForReview(token.uid)
        return ApiResponse.success(response)
    }

    @GetMapping("/presigned-put-url")
    fun getPresignedPutUrl(
        @RequestParam("folder", defaultValue = "partners") folder: String,
        @RequestParam("extension") extension: String,
        @RequestParam("contentType") contentType: String
    ): ApiResponse<Map<String, String>> {
        val data = s3Service.generatePresignedPutUrl(folder, extension, contentType)
        return ApiResponse.success(data)
    }
}
