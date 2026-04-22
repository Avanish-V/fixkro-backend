package com.iotabuild.zippyhand_Admin.offers.service

import com.iotabuild.zippyhand_Admin.offers.dto.CreateOfferRequest
import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import com.iotabuild.zippyhand_Admin.offers.entity.OfferEntity
import com.iotabuild.zippyhand_Admin.offers.entity.OfferStatus
import com.iotabuild.zippyhand_Admin.offers.repository.OfferRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminOfferService(
    private val offerRepository: OfferRepository,
    private val queryService: OfferQueryService
) {

    @Transactional
    fun createOffer(request: CreateOfferRequest): OfferResponse {
        if (offerRepository.existsByOfferCodeIgnoreCase(request.offerCode)) {
            throw RuntimeException("Offer with code ${request.offerCode} already exists")
        }

        val offer = OfferEntity(
            offerCode = request.offerCode.uppercase(),
            shortDescription = request.shortDescription,
            categoryId = request.categoryId,
            productId = request.productId,
            discountType = request.discountType,
            discountValue = request.discountValue,
            expiryDate = request.expiryDate,
            usageLimit = request.limit,
            userType = request.userType,
            status = OfferStatus.ACTIVE
        )

        val saved = offerRepository.save(offer)
        return queryService.getOfferById(saved.id)
    }

    @Transactional
    fun updateOffer(id: Long, request: CreateOfferRequest): OfferResponse {
        val existing = queryService.getOfferEntityById(id)
        
        val updated = existing.copy(
            offerCode = request.offerCode.uppercase(),
            shortDescription = request.shortDescription,
            categoryId = request.categoryId,
            productId = request.productId,
            discountType = request.discountType,
            discountValue = request.discountValue,
            expiryDate = request.expiryDate,
            usageLimit = request.limit,
            userType = request.userType,
            updatedAt = LocalDateTime.now()
        )

        val saved = offerRepository.save(updated)
        return queryService.getOfferById(saved.id)
    }

    @Transactional
    fun toggleStatus(id: Long): OfferResponse {
        val existing = queryService.getOfferEntityById(id)
        val newStatus = if (existing.status == OfferStatus.ACTIVE) OfferStatus.INACTIVE else OfferStatus.ACTIVE
        
        val updated = existing.copy(
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = offerRepository.save(updated)
        return queryService.getOfferById(saved.id)
    }

    @Transactional
    fun deleteOffer(id: Long) {
        if (!offerRepository.existsById(id)) {
            throw RuntimeException("Offer not found")
        }
        offerRepository.deleteById(id)
    }
}
