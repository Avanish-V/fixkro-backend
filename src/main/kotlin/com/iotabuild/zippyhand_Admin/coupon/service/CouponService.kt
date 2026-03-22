package com.iotabuild.zippyhand_Admin.coupon.service

import com.iotabuild.zippyhand_Admin.coupon.*
import com.iotabuild.zippyhand_Admin.coupon.models.*
import com.iotabuild.zippyhand_Admin.coupon.repository.CouponRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * Service for coupon management and validation
 */
@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val offerRepository: com.iotabuild.zippyhand_Admin.offers.OfferRepository
) {
    
    private val logger = LoggerFactory.getLogger(CouponService::class.java)
    
    /**
     * Create a new coupon
     */
    @Transactional
    fun createCoupon(request: CreateCouponRequest): CouponResponse {
        logger.info("Creating coupon with code: ${request.code}")
        
        // Validate coupon code uniqueness
        if (couponRepository.existsByCodeIgnoreCase(request.code)) {
            throw DuplicateCouponCodeException(request.code)
        }
        
        // Validate dates
        if (request.validFrom.isAfter(request.validUntil)) {
            throw InvalidCouponException("Valid from date must be before valid until date")
        }
        
        // Validate percentage discount
        if (request.discountType == DiscountType.PERCENTAGE && request.discountValue > BigDecimal(100)) {
            throw InvalidCouponException("Percentage discount cannot exceed 100%")
        }
        
        val coupon = CouponEntity(
            code = request.code.uppercase(),
            discountType = request.discountType,
            discountValue = request.discountValue,
            minOrderValue = request.minOrderValue,
            maxDiscountAmount = request.maxDiscountAmount,
            validFrom = request.validFrom,
            validUntil = request.validUntil,
            totalUsageLimit = request.totalUsageLimit,
            perUserUsageLimit = request.perUserUsageLimit,
            applicableProductIds = request.applicableProductIds,
            applicableCategoryIds = request.applicableCategoryIds,
            isActive = request.isActive,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val saved = couponRepository.save(coupon)
        logger.info("Coupon created successfully: ${saved.code}")
        
        return mapToResponse(saved)
    }
    
    /**
     * Get coupon by code
     */
    fun getCouponByCode(code: String): CouponResponse {
        logger.debug("Fetching coupon: $code")
        val coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow { CouponNotFoundException(code) }
        return mapToResponse(coupon)
    }
    
    /**
     * Get all coupons
     */
    fun getAllCoupons(): List<CouponSummaryResponse> {
        logger.debug("Fetching all coupons")
        return couponRepository.findAllByOrderByCreatedAtDesc()
            .map { mapToSummaryResponse(it) }
    }
    
    /**
     * Get active coupons
     */
    fun getActiveCoupons(): List<CouponSummaryResponse> {
        logger.debug("Fetching active coupons")
        return couponRepository.findAllActiveCoupons(LocalDateTime.now())
            .map { mapToSummaryResponse(it) }
    }
    
    /**
     * Update coupon
     */
    @Transactional
    fun updateCoupon(code: String, request: UpdateCouponRequest): CouponResponse {
        logger.info("Updating coupon: $code")
        
        val coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow { CouponNotFoundException(code) }
        
        val updated = coupon.copy(
            discountValue = request.discountValue ?: coupon.discountValue,
            minOrderValue = request.minOrderValue ?: coupon.minOrderValue,
            maxDiscountAmount = request.maxDiscountAmount ?: coupon.maxDiscountAmount,
            validUntil = request.validUntil ?: coupon.validUntil,
            totalUsageLimit = request.totalUsageLimit ?: coupon.totalUsageLimit,
            isActive = request.isActive ?: coupon.isActive,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = couponRepository.save(updated)
        logger.info("Coupon updated successfully: ${saved.code}")
        
        return mapToResponse(saved)
    }
    
    /**
     * Deactivate coupon
     */
    @Transactional
    fun deactivateCoupon(code: String): CouponResponse {
        logger.info("Deactivating coupon: $code")
        
        val coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow { CouponNotFoundException(code) }
        
        val deactivated = coupon.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = couponRepository.save(deactivated)
        logger.info("Coupon deactivated: ${saved.code}")
        
        return mapToResponse(saved)
    }
    
    /**
     * Validate and calculate discount for a coupon
     */
    fun validateAndCalculateDiscount(
        code: String,
        productId: Long,
        categoryId: Long?,
        orderAmount: BigDecimal
    ): CouponValidationResult {
        logger.debug("Validating coupon: $code for product: $productId, amount: $orderAmount")
        
        val couponOpt = couponRepository.findByCodeIgnoreCase(code)
        
        if (couponOpt.isPresent) {
            val coupon = couponOpt.get()
            // Check if active
            if (!coupon.isActive) {
                return CouponValidationResult(isValid = false, errorMessage = "Coupon is not active")
            }
            
            // Check expiry
            val now = LocalDateTime.now()
            if (now.isBefore(coupon.validFrom) || now.isAfter(coupon.validUntil)) {
                return CouponValidationResult(isValid = false, errorMessage = "Coupon has expired or not yet valid")
            }
            
            // Check usage limit
            if (coupon.totalUsageLimit != null && coupon.currentUsageCount >= coupon.totalUsageLimit) {
                return CouponValidationResult(isValid = false, errorMessage = "Coupon usage limit reached")
            }
            
            // Check minimum order value
            if (coupon.minOrderValue != null && orderAmount < coupon.minOrderValue) {
                return CouponValidationResult(isValid = false, errorMessage = "Minimum order value of ${coupon.minOrderValue} not met")
            }
            
            // Check product applicability
            if (coupon.applicableProductIds != null && !coupon.applicableProductIds.contains(productId)) {
                return CouponValidationResult(isValid = false, errorMessage = "Coupon not applicable to this product")
            }
            
            // Check category applicability
            if (coupon.applicableCategoryIds != null && categoryId != null && 
                !coupon.applicableCategoryIds.contains(categoryId)) {
                return CouponValidationResult(isValid = false, errorMessage = "Coupon not applicable to this category")
            }
            
            val discount = calculateDiscount(coupon, orderAmount)
            return CouponValidationResult(isValid = true, coupon = coupon, calculatedDiscount = discount)
        }

        // Fallback to Offers table (Admin Hub creates items here)
        val offerOpt = offerRepository.findByCouponCodeIgnoreCase(code)
        if (offerOpt.isPresent) {
            val offer = offerOpt.get()
            
            if (offer.status != com.iotabuild.zippyhand_Admin.offers.OfferStatus.ACTIVE) {
                return CouponValidationResult(isValid = false, errorMessage = "Offer is not active")
            }

            if (LocalDateTime.now().isAfter(offer.expiryDate)) {
                return CouponValidationResult(isValid = false, errorMessage = "Offer has expired")
            }

            // Check product/category applicability for Offer
            if (offer.productId != null && offer.productId != productId) {
                return CouponValidationResult(isValid = false, errorMessage = "Offer not applicable to this product")
            }

            if (offer.categoryId != null && offer.categoryId != categoryId) {
                return CouponValidationResult(isValid = false, errorMessage = "Offer not applicable to this category")
            }

            val discount = when (offer.discountType) {
                com.iotabuild.zippyhand_Admin.offers.OfferDiscountType.PERCENTAGE -> {
                    (orderAmount * offer.discountValue / BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                }
                com.iotabuild.zippyhand_Admin.offers.OfferDiscountType.FIXED -> {
                    offer.discountValue
                }
            }

            return CouponValidationResult(
                isValid = true,
                calculatedDiscount = discount.min(orderAmount)
            )
        }

        return CouponValidationResult(isValid = false, errorMessage = "Invalid coupon code")
    }
    
    /**
     * Increment coupon usage count
     */
    @Transactional
    fun incrementUsageCount(code: String) {
        logger.debug("Incrementing usage count for coupon: $code")
        
        val couponOpt = couponRepository.findByCodeIgnoreCase(code)
        
        if (couponOpt.isPresent) {
            val coupon = couponOpt.get()
            val updated = coupon.copy(
                currentUsageCount = coupon.currentUsageCount + 1,
                updatedAt = LocalDateTime.now()
            )
            couponRepository.save(updated)
            logger.debug("Usage count incremented for coupon: $code")
        } else {
            logger.info("Coupon code $code not found in coupon repository, skipping usage increment")
        }
    }
    
    /**
     * Calculate discount amount based on coupon type
     */
    private fun calculateDiscount(coupon: CouponEntity, orderAmount: BigDecimal): BigDecimal {
        val discount = when (coupon.discountType) {
            DiscountType.PERCENTAGE -> {
                (orderAmount * coupon.discountValue / BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            }
            DiscountType.FIXED_AMOUNT -> {
                coupon.discountValue
            }
        }
        
        // Apply max discount cap if specified
        val finalDiscount = if (coupon.maxDiscountAmount != null) {
            discount.min(coupon.maxDiscountAmount)
        } else {
            discount
        }
        
        // Ensure discount doesn't exceed order amount
        return finalDiscount.min(orderAmount)
    }
    
    /**
     * Map entity to response
     */
    private fun mapToResponse(coupon: CouponEntity): CouponResponse {
        val now = LocalDateTime.now()
        return CouponResponse(
            couponId = coupon.couponId,
            code = coupon.code,
            discountType = coupon.discountType,
            discountValue = coupon.discountValue,
            minOrderValue = coupon.minOrderValue,
            maxDiscountAmount = coupon.maxDiscountAmount,
            validFrom = coupon.validFrom,
            validUntil = coupon.validUntil,
            totalUsageLimit = coupon.totalUsageLimit,
            perUserUsageLimit = coupon.perUserUsageLimit,
            currentUsageCount = coupon.currentUsageCount,
            applicableProductIds = coupon.applicableProductIds,
            applicableCategoryIds = coupon.applicableCategoryIds,
            isActive = coupon.isActive,
            isExpired = now.isAfter(coupon.validUntil),
            isUsageLimitReached = coupon.totalUsageLimit != null && 
                                  coupon.currentUsageCount >= coupon.totalUsageLimit,
            createdAt = coupon.createdAt,
            updatedAt = coupon.updatedAt
        )
    }
    
    /**
     * Map entity to summary response
     */
    private fun mapToSummaryResponse(coupon: CouponEntity): CouponSummaryResponse {
        val now = LocalDateTime.now()
        return CouponSummaryResponse(
            couponId = coupon.couponId,
            code = coupon.code,
            discountType = coupon.discountType,
            discountValue = coupon.discountValue,
            validUntil = coupon.validUntil,
            currentUsageCount = coupon.currentUsageCount,
            totalUsageLimit = coupon.totalUsageLimit,
            isActive = coupon.isActive,
            isExpired = now.isAfter(coupon.validUntil)
        )
    }
}
