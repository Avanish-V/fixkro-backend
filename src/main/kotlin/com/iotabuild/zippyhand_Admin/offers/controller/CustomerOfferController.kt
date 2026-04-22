package com.iotabuild.zippyhand_Admin.offers.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import com.iotabuild.zippyhand_Admin.offers.service.CustomerOfferService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/offers")
class CustomerOfferController(
    private val customerOfferService: CustomerOfferService
) {

    @GetMapping
    fun getAllActiveOffers(): ApiResponse<List<OfferResponse>> {
        return ApiResponse.success(customerOfferService.getAllActiveOffers())
    }

    @GetMapping("/product/{productId}")
    fun getOffersByProduct(@PathVariable productId: Long): ApiResponse<List<OfferResponse>> {
        return ApiResponse.success(customerOfferService.getOffersByProduct(productId))
    }

    @GetMapping("/{id}")
    fun getOfferById(@PathVariable id: Long): ApiResponse<OfferResponse> {
        return ApiResponse.success(customerOfferService.getOfferById(id))
    }
}
