package com.iotabuild.zippyhand_Admin.offers.service

import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import org.springframework.stereotype.Service

@Service
class CustomerOfferService(
    private val queryService: OfferQueryService
) {

    fun getOffersByProduct(productId: Long): List<OfferResponse> {
        return queryService.getOffersByProduct(productId)
    }

    fun getAllActiveOffers(): List<OfferResponse> {
        // We could filter queryService.getAllOffers() for ACTIVE only if needed
        return queryService.getAllOffers().filter { it.status == com.iotabuild.zippyhand_Admin.offers.entity.OfferStatus.ACTIVE }
    }
    
    fun getOfferById(id: Long): OfferResponse {
        return queryService.getOfferById(id)
    }
}
