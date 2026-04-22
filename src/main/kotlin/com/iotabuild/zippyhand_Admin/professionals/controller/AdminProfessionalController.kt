package com.iotabuild.zippyhand_Admin.professionals.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.professionals.dto.*
import com.iotabuild.zippyhand_Admin.professionals.service.AdminProfessionalService
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalQueryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/professionals")
class AdminProfessionalController(
    private val adminProfessionalService: AdminProfessionalService,
    private val queryService: ProfessionalQueryService
) {

    @GetMapping
    fun getAllProfessionals(): ApiResponse<List<ProfessionalResponse>> {
        return ApiResponse.success(queryService.getAllProfessionals())
    }

    @GetMapping("/{id}")
    fun getProfessionalById(@PathVariable id: Long): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(queryService.getProfessionalById(id))
    }

    @GetMapping("/search")
    fun searchProfessionals(@RequestParam query: String): ApiResponse<List<ProfessionalResponse>> {
        return ApiResponse.success(queryService.searchProfessionals(query))
    }

    @PostMapping
    fun createProfessional(@Valid @RequestBody request: CreateProfessionalRequest): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(adminProfessionalService.createProfessional(request))
    }

    @PutMapping("/{id}")
    fun updateProfessional(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProfessionalRequest
    ): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(adminProfessionalService.updateProfessional(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteProfessional(@PathVariable id: Long): ApiResponse<String> {
        adminProfessionalService.deleteProfessional(id)
        return ApiResponse.success("Professional deleted successfully")
    }
}
