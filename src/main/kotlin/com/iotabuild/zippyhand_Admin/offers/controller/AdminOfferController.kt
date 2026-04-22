package com.iotabuild.zippyhand_Admin.offers.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.offers.dto.CreateOfferRequest
import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import com.iotabuild.zippyhand_Admin.offers.service.AdminOfferService
import com.iotabuild.zippyhand_Admin.offers.service.OfferQueryService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/offers")
class AdminOfferController(
    private val adminOfferService: AdminOfferService,
    private val queryService: OfferQueryService
) {

    @GetMapping
    fun getAllOffers(): ApiResponse<List<OfferResponse>> {
        return ApiResponse.success(queryService.getAllOffers())
    }

    @GetMapping("/{id}")
    fun getOfferById(@PathVariable id: Long): ApiResponse<OfferResponse> {
        return ApiResponse.success(queryService.getOfferById(id))
    }

    @PostMapping
    fun createOffer(@RequestBody request: CreateOfferRequest): ApiResponse<OfferResponse> {
        return ApiResponse.success(adminOfferService.createOffer(request))
    }

    @PutMapping("/{id}")
    fun updateOffer(@PathVariable id: Long, @RequestBody request: CreateOfferRequest): ApiResponse<OfferResponse> {
        return ApiResponse.success(adminOfferService.updateOffer(id, request))
    }

    @PatchMapping("/{id}/toggle")
    fun toggleStatus(@PathVariable id: Long): ApiResponse<OfferResponse> {
        return ApiResponse.success(adminOfferService.toggleStatus(id))
    }

    @DeleteMapping("/{id}")
    fun deleteOffer(@PathVariable id: Long): ApiResponse<String> {
        adminOfferService.deleteOffer(id)
        return ApiResponse.success("Offer deleted successfully")
    }
}
