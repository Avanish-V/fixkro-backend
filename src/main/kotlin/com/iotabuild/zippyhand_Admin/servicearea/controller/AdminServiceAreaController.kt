package com.iotabuild.zippyhand_Admin.servicearea.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.servicearea.dto.*
import com.iotabuild.zippyhand_Admin.servicearea.service.ServiceAreaService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/service-availability")
class AdminServiceAreaController(
    private val serviceAreaService: ServiceAreaService
) {

    @GetMapping
    fun getAllServiceAreas(
        @RequestParam(value = "city", required = false) city: String?,
        @RequestParam(value = "activeOnly", required = false) activeOnly: Boolean?
    ): ApiResponse<List<ServiceAreaResponse>> {
        return ApiResponse.success(serviceAreaService.getAllServiceAreas(city, activeOnly))
    }

    @GetMapping("/stats")
    fun getStats(): ApiResponse<ServiceAreaStatsResponse> {
        return ApiResponse.success(serviceAreaService.getStats())
    }

    @GetMapping("/{id}")
    fun getServiceAreaById(@PathVariable id: Long): ApiResponse<ServiceAreaResponse> {
        return ApiResponse.success(serviceAreaService.getServiceAreaById(id))
    }

    @PostMapping
    fun createServiceArea(
        @Valid @RequestBody request: ServiceAreaRequest
    ): ApiResponse<ServiceAreaResponse> {
        return ApiResponse.success(serviceAreaService.createServiceArea(request))
    }

    @PutMapping("/{id}")
    fun updateServiceArea(
        @PathVariable id: Long,
        @Valid @RequestBody request: ServiceAreaRequest
    ): ApiResponse<ServiceAreaResponse> {
        return ApiResponse.success(serviceAreaService.updateServiceArea(id, request))
    }

    @PatchMapping("/{id}/status")
    fun toggleStatus(
        @PathVariable id: Long,
        @RequestParam("active") active: Boolean
    ): ApiResponse<ServiceAreaResponse> {
        return ApiResponse.success(serviceAreaService.toggleServiceAreaStatus(id, active))
    }

    @DeleteMapping("/{id}")
    fun deleteServiceArea(@PathVariable id: Long): ApiResponse<String> {
        serviceAreaService.deleteServiceArea(id)
        return ApiResponse.success("Service area deleted successfully")
    }

    @PostMapping("/bulk")
    fun bulkCreateServiceAreas(
        @Valid @RequestBody request: BulkServiceAreaRequest
    ): ApiResponse<List<ServiceAreaResponse>> {
        return ApiResponse.success(serviceAreaService.bulkCreateServiceAreas(request))
    }
}
