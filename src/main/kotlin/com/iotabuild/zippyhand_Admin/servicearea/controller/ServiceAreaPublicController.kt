package com.iotabuild.zippyhand_Admin.servicearea.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.servicearea.dto.PincodeAvailabilityResponse
import com.iotabuild.zippyhand_Admin.servicearea.dto.ServiceAreaResponse
import com.iotabuild.zippyhand_Admin.servicearea.service.ServiceAreaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/service-availability")
class ServiceAreaPublicController(
    private val serviceAreaService: ServiceAreaService
) {

    /**
     * Check if a specific 6-digit pincode has active service availability.
     * Used by mobile client and customer website before booking.
     */
    @GetMapping("/check")
    fun checkAvailability(
        @RequestParam("pincode") pincode: String
    ): ApiResponse<PincodeAvailabilityResponse> {
        return ApiResponse.success(serviceAreaService.checkAvailability(pincode))
    }

    /**
     * Get all currently active serviceable pincodes list.
     */
    @GetMapping("/pincodes")
    fun getActivePincodes(): ApiResponse<List<String>> {
        return ApiResponse.success(serviceAreaService.getActivePincodes())
    }

    /**
     * Get active service areas filtered by city or all.
     */
    @GetMapping("/areas")
    fun getActiveAreas(
        @RequestParam(value = "city", required = false) city: String?
    ): ApiResponse<List<ServiceAreaResponse>> {
        return ApiResponse.success(serviceAreaService.getAllServiceAreas(city = city, activeOnly = true))
    }
}
