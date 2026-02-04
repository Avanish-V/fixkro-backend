package com.iotabuild.zippyhand_Admin.professionals.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.professionals.models.*
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/professionals")
class ProfessionalController(private val professionalService: ProfessionalService) {

    @GetMapping
    fun getAllProfessionals(): ApiResponse<List<ProfessionalResponse>> {
        return ApiResponse.success(professionalService.getAllProfessionals())
    }

    @GetMapping("/{id}")
    fun getProfessionalById(@PathVariable id: Long): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(professionalService.getProfessionalById(id))
    }

    @GetMapping("/search")
    fun searchProfessionals(@RequestParam query: String): ApiResponse<List<ProfessionalResponse>> {
        return ApiResponse.success(professionalService.searchProfessionals(query))
    }

    @PostMapping
    fun createProfessional(@Valid @RequestBody request: CreateProfessionalRequest): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(professionalService.createProfessional(request))
    }

    @PutMapping("/{id}")
    fun updateProfessional(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProfessionalRequest
    ): ApiResponse<ProfessionalResponse> {
        return ApiResponse.success(professionalService.updateProfessional(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteProfessional(@PathVariable id: Long): ApiResponse<String> {
        professionalService.deleteProfessional(id)
        return ApiResponse.success("Professional deleted successfully")
    }
}
