package com.iotabuild.zippyhand_Admin.pricing

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

/**
 * Request model for price calculation
 */
data class PriceRequest(
    @field:NotNull(message = "Product ID is required")
    @field:Positive(message = "Product ID must be positive")
    val productId: Long,
    
    @field:Positive(message = "Quantity must be positive")
    val quantity: Int = 1,
    
    val couponCode: String? = null
)

/**
 * Response model containing detailed price breakdown
 */
data class PriceDetailResponse(
    val basePrice: BigDecimal,
    val taxAmount: BigDecimal,
    val discount: BigDecimal,
    val appliedCoupon: String?,
    val totalPrice: BigDecimal,
    val currency: String = "INR"
)

/**
 * Internal model for price calculation breakdown
 */
internal data class PriceBreakdown(
    val basePrice: BigDecimal,
    val taxAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalPrice: BigDecimal
)
