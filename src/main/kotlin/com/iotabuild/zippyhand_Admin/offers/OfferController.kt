package com.iotabuild.zippyhand_Admin.offers

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.payment.PaymentController
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/offers")
class OfferController(private val offerService: OfferService) {


    private val logger = LoggerFactory.getLogger(OfferController::class.java)

    @GetMapping
    fun getAllOffers(): ApiResponse<List<OfferResponse>> {
        return ApiResponse.success(offerService.getAllOffers())
    }

    @GetMapping("/product/{productId}")
    fun getOffersByProduct(@PathVariable productId: Long): ApiResponse<List<OfferResponse>> {
        return ApiResponse.success(offerService.getOffersByProduct(productId))
    }

    @GetMapping("/{id}")
    fun getOfferById(@PathVariable id: Long): ApiResponse<OfferResponse> {
        return ApiResponse.success(offerService.getOfferById(id))
    }

    @PostMapping
    fun createOffer(@RequestBody request: CreateOfferRequest): ApiResponse<OfferResponse> {
        return ApiResponse.success(offerService.createOffer(request))
    }

    @PutMapping("/{id}")
    fun updateOffer(@PathVariable id: Long, @RequestBody request: CreateOfferRequest): ApiResponse<OfferResponse> {
        return ApiResponse.success(offerService.updateOffer(id, request))
    }

    @PatchMapping("/{id}/toggle")
    fun toggleStatus(@PathVariable id: Long): ApiResponse<OfferResponse> {
        return ApiResponse.success(offerService.toggleStatus(id))
    }

    @DeleteMapping("/{id}")
    fun deleteOffer(@PathVariable id: Long): ApiResponse<String> {
        offerService.deleteOffer(id)
        return ApiResponse.success("Offer deleted successfully")
    }
}
