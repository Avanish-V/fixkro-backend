package com.iotabuild.zippyhand_Admin.offers.service

import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import com.iotabuild.zippyhand_Admin.offers.dto.OfferValidationResponse
import com.iotabuild.zippyhand_Admin.offers.entity.OfferDiscountType
import com.iotabuild.zippyhand_Admin.offers.entity.OfferEntity
import com.iotabuild.zippyhand_Admin.offers.mapper.OfferMapper
import com.iotabuild.zippyhand_Admin.offers.repository.OfferRepository
import com.iotabuild.zippyhand_Admin.products.service.ProductQueryService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class OfferQueryService(
    private val offerRepository: OfferRepository,
    private val productQueryService: ProductQueryService,
    private val offerMapper: OfferMapper
) {

    fun getAllOffers(): List<OfferResponse> {
        val offers = offerRepository.findAllByOrderByCreatedAtDesc()
        
        // Following loose coupling: using Service instead of Repository
        val products = productQueryService.getAllProducts().associateBy { it.productId }
        val categories = productQueryService.getAllCategories().associateBy { it.categoryId }

        return offers.map { offer ->
            val productResponse = products[offer.productId]
            val categoryResponse = categories[offer.categoryId]
            
            // Map using response data
            OfferResponse(
                id = offer.id,
                offerCode = offer.offerCode,
                shortDescription = offer.shortDescription,
                categoryId = offer.categoryId,
                categoryName = categoryResponse?.title ?: if (offer.categoryId == null) "All Categories" else "Unknown Category",
                productId = offer.productId,
                productName = productResponse?.title ?: if (offer.productId == null) "All Products" else "Unknown Product",
                discountType = offer.discountType,
                discountValue = offer.discountValue,
                expiryDate = offer.expiryDate,
                limit = offer.usageLimit,
                userType = offer.userType,
                status = offer.status,
                createdAt = offer.createdAt,
                updatedAt = offer.updatedAt
            )
        }
    }

    fun getOffersByProduct(productId: Long): List<OfferResponse> {
        val product = productQueryService.getProduct(productId)
        
        val activeOffers = offerRepository.findActiveOffersForProduct(
            productId = product.productId,
            categoryId = product.categoryId,
            now = LocalDateTime.now()
        )

        val categories = productQueryService.getAllCategories().associateBy { it.categoryId }

        return activeOffers.map { offer ->
            val categoryResponse = categories[offer.categoryId]
            OfferResponse(
                id = offer.id,
                offerCode = offer.offerCode,
                shortDescription = offer.shortDescription,
                categoryId = offer.categoryId,
                categoryName = categoryResponse?.title ?: if (offer.categoryId == null) "All Categories" else "Unknown Category",
                productId = offer.productId,
                productName = product.title,
                discountType = offer.discountType,
                discountValue = offer.discountValue,
                expiryDate = offer.expiryDate,
                limit = offer.usageLimit,
                userType = offer.userType,
                status = offer.status,
                createdAt = offer.createdAt,
                updatedAt = offer.updatedAt
            )
        }
    }

    fun getOfferById(id: Long): OfferResponse {
        val offer = offerRepository.findById(id).orElseThrow { RuntimeException("Offer not found") }
        val product = offer.productId?.let { try { productQueryService.getProduct(it) } catch (e: Exception) { null } }
        val category = offer.categoryId?.let { try { productQueryService.getCategory(it) } catch (e: Exception) { null } }
        
        return OfferResponse(
            id = offer.id,
            offerCode = offer.offerCode,
            shortDescription = offer.shortDescription,
            categoryId = offer.categoryId,
            categoryName = category?.title ?: if (offer.categoryId == null) "All Categories" else "Unknown Category",
            productId = offer.productId,
            productName = product?.title ?: if (offer.productId == null) "All Products" else "Unknown Product",
            discountType = offer.discountType,
            discountValue = offer.discountValue,
            expiryDate = offer.expiryDate,
            limit = offer.usageLimit,
            userType = offer.userType,
            status = offer.status,
            createdAt = offer.createdAt,
            updatedAt = offer.updatedAt
        )
    }
    
    fun getOfferEntityById(id: Long): OfferEntity {
        return offerRepository.findById(id).orElseThrow { RuntimeException("Offer not found") }
    }

    fun validateAndCalculateOffer(
        code: String,
        productId: Long,
        categoryId: Long,
        orderAmount: BigDecimal
    ): OfferValidationResponse {
        val offer = offerRepository.findByOfferCodeIgnoreCase(code).orElse(null)
            ?: return OfferValidationResponse(false, errorMessage = "Offer code not found")

        if (offer.status != com.iotabuild.zippyhand_Admin.offers.entity.OfferStatus.ACTIVE) {
            return OfferValidationResponse(false, errorMessage = "Offer is not active")
        }

        if (offer.expiryDate.isBefore(LocalDateTime.now())) {
            return OfferValidationResponse(false, errorMessage = "Offer has expired")
        }

        // Check product/category applicability
        if (offer.productId != null && offer.productId != productId) {
            return OfferValidationResponse(false, errorMessage = "This offer is not valid for this product")
        }

        if (offer.categoryId != null && offer.categoryId != categoryId) {
            return OfferValidationResponse(false, errorMessage = "This offer is not valid for this category")
        }

        val discount = when (offer.discountType) {
            OfferDiscountType.FIXED -> offer.discountValue
            OfferDiscountType.PERCENTAGE -> {
                orderAmount.multiply(offer.discountValue)
                    .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
            }
        }

        return OfferValidationResponse(
            isValid = true,
            calculatedDiscount = discount.min(orderAmount)
        )
    }
}
