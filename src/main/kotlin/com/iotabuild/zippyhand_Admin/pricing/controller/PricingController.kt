package com.iotabuild.zippyhand_Admin.pricing.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceDetailResponse
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.service.PricingService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * Controller for price-related operations
 */
@RestController
@RequestMapping("/api/v1/pricing")
class PricingController(
    private val pricingService: PricingService
) {
    
    /**
     * Calculate detailed price breakdown for a product
     */
    @PostMapping("/calculate")
    fun calculatePrice(@Valid @RequestBody request: PriceRequest): ApiResponse<PriceDetailResponse> {
        val response = pricingService.calculateFinalPrice(request)
        return ApiResponse.success(response)
    }
}
