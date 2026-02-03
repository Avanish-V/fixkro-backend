package com.iotabuild.zippyhand_Admin.coupon.models

import com.iotabuild.zippyhand_Admin.coupon.models.DiscountType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Request to create a new coupon
 */
data class CreateCouponRequest(
    @field:NotBlank(message = "Coupon code is required")
    @field:Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    @field:Pattern(regexp = "^[A-Z0-9_-]+$", message = "Coupon code must contain only uppercase letters, numbers, hyphens, and underscores")
    val code: String,
    
    @field:NotNull(message = "Discount type is required")
    val discountType: DiscountType,
    
    @field:NotNull(message = "Discount value is required")
    @field:Positive(message = "Discount value must be positive")
    val discountValue: BigDecimal,
    
    @field:PositiveOrZero(message = "Minimum order value must be non-negative")
    val minOrderValue: BigDecimal? = null,
    
    @field:Positive(message = "Maximum discount amount must be positive")
    val maxDiscountAmount: BigDecimal? = null,
    
    @field:NotNull(message = "Valid from date is required")
    val validFrom: LocalDateTime,
    
    @field:NotNull(message = "Valid until date is required")
    val validUntil: LocalDateTime,
    
    @field:Positive(message = "Total usage limit must be positive")
    val totalUsageLimit: Int? = null,
    
    @field:Positive(message = "Per user usage limit must be positive")
    val perUserUsageLimit: Int = 1,
    
    val applicableProductIds: List<Long>? = null,
    
    val applicableCategoryIds: List<Long>? = null,
    
    val isActive: Boolean = true
)

/**
 * Request to update an existing coupon
 */
data class UpdateCouponRequest(
    @field:Positive(message = "Discount value must be positive")
    val discountValue: BigDecimal? = null,
    
    @field:PositiveOrZero(message = "Minimum order value must be non-negative")
    val minOrderValue: BigDecimal? = null,
    
    @field:Positive(message = "Maximum discount amount must be positive")
    val maxDiscountAmount: BigDecimal? = null,
    
    val validUntil: LocalDateTime? = null,
    
    @field:Positive(message = "Total usage limit must be positive")
    val totalUsageLimit: Int? = null,
    
    val isActive: Boolean? = null
)

/**
 * Complete coupon response
 */
data class CouponResponse(
    val couponId: Long,
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val minOrderValue: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime,
    val totalUsageLimit: Int?,
    val perUserUsageLimit: Int,
    val currentUsageCount: Int,
    val applicableProductIds: List<Long>?,
    val applicableCategoryIds: List<Long>?,
    val isActive: Boolean,
    val isExpired: Boolean,
    val isUsageLimitReached: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Summary response for listing coupons
 */
data class CouponSummaryResponse(
    val couponId: Long,
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val validUntil: LocalDateTime,
    val currentUsageCount: Int,
    val totalUsageLimit: Int?,
    val isActive: Boolean,
    val isExpired: Boolean
)

/**
 * Coupon validation result
 */
data class CouponValidationResult(
    val isValid: Boolean,
    val coupon: CouponEntity? = null,
    val errorMessage: String? = null,
    val calculatedDiscount: BigDecimal = BigDecimal.ZERO
)
