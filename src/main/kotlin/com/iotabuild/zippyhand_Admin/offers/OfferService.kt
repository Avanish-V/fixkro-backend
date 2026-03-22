package com.iotabuild.zippyhand_Admin.offers

import com.iotabuild.zippyhand_Admin.products.repository.CategoryRepository
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

data class OfferResponse(
    val id: Long,
    val couponCode: String,
    val shortDescription: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val productId: Long?,
    val productName: String?,
    val discountType: OfferDiscountType,
    val discountValue: BigDecimal,
    val expiryDate: LocalDateTime,
    val limit: Int,
    val userType: OfferUserType,
    val status: OfferStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateOfferRequest(
    val couponCode: String,
    val shortDescription: String?,
    val categoryId: Long?,
    val productId: Long?,
    val discountType: OfferDiscountType,
    val discountValue: BigDecimal,
    val expiryDate: LocalDateTime,
    val limit: Int,
    val userType: OfferUserType
)

@Service
class OfferService(
    private val offerRepository: OfferRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    fun getAllOffers(): List<OfferResponse> {
        val offers = offerRepository.findAllByOrderByCreatedAtDesc()
        val products = productRepository.findAll().associateBy { it.productId }
        val categories = categoryRepository.findAll().associateBy { it.categoryId }

        return offers.map { offer ->
            mapToResponse(offer, products, categories)
        }
    }

    fun getOffersByProduct(productId: Long): List<OfferResponse> {
        val product = productRepository.findById(productId)
            .orElseThrow { RuntimeException("Product not found") }
        
        val activeOffers = offerRepository.findActiveOffersForProduct(
            productId = product.productId,
            categoryId = product.categoryId,
            now = LocalDateTime.now()
        )

        val products = mapOf(product.productId to product)
        val categories = categoryRepository.findAll().associateBy { it.categoryId }

        return activeOffers.map { offer ->
            mapToResponse(offer, products, categories)
        }
    }

    fun getOfferById(id: Long): OfferResponse {
        val offer = offerRepository.findById(id).orElseThrow { RuntimeException("Offer not found") }
        val product = offer.productId?.let { productRepository.findById(it).orElse(null) }
        val category = offer.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        
        return mapToResponse(offer, 
            product?.let { mapOf(it.productId to it) } ?: emptyMap(),
            category?.let { mapOf(it.categoryId to it) } ?: emptyMap()
        )
    }

    @Transactional
    fun createOffer(request: CreateOfferRequest): OfferResponse {
        if (offerRepository.existsByCouponCodeIgnoreCase(request.couponCode)) {
            throw RuntimeException("Offer with code ${request.couponCode} already exists")
        }

        val offer = OfferEntity(
            couponCode = request.couponCode.uppercase(),
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
        return getOfferById(saved.id)
    }

    @Transactional
    fun updateOffer(id: Long, request: CreateOfferRequest): OfferResponse {
        val existing = offerRepository.findById(id).orElseThrow { RuntimeException("Offer not found") }
        
        val updated = existing.copy(
            couponCode = request.couponCode.uppercase(),
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
        return getOfferById(saved.id)
    }

    @Transactional
    fun toggleStatus(id: Long): OfferResponse {
        val existing = offerRepository.findById(id).orElseThrow { RuntimeException("Offer not found") }
        val newStatus = if (existing.status == OfferStatus.ACTIVE) OfferStatus.INACTIVE else OfferStatus.ACTIVE
        
        val updated = existing.copy(
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = offerRepository.save(updated)
        return getOfferById(saved.id)
    }

    @Transactional
    fun deleteOffer(id: Long) {
        offerRepository.deleteById(id)
    }

    private fun mapToResponse(
        offer: OfferEntity, 
        products: Map<Long, com.iotabuild.zippyhand_Admin.products.models.ProductEntity>,
        categories: Map<Long, com.iotabuild.zippyhand_Admin.products.models.CategoryEntity>
    ): OfferResponse {
        return OfferResponse(
            id = offer.id,
            couponCode = offer.couponCode,
            shortDescription = offer.shortDescription,
            categoryId = offer.categoryId,
            categoryName = if (offer.categoryId == null) "All Categories" else categories[offer.categoryId]?.title ?: "Unknown Category",
            productId = offer.productId,
            productName = if (offer.productId == null) "All Products" else products[offer.productId]?.title ?: "Unknown Product",
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
