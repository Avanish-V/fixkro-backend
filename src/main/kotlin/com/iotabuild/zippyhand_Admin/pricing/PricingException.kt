package com.iotabuild.zippyhand_Admin.pricing

/**
 * Base exception for pricing-related errors
 */
sealed class PricingException(message: String) : RuntimeException(message)

/**
 * Thrown when a product is not found
 */
class ProductNotFoundException(productId: Long) : 
    PricingException("Product not found with ID: $productId")

/**
 * Thrown when an invalid or expired coupon is used
 */
class InvalidCouponException(couponCode: String) : 
    PricingException("Invalid or expired coupon code: $couponCode")

/**
 * Thrown when price calculation fails
 */
class PricingCalculationException(message: String, cause: Throwable? = null) : 
    PricingException(message) {
    init {
        cause?.let { initCause(it) }
    }
}
