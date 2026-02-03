package com.iotabuild.zippyhand_Admin.coupon

/**
 * Base exception for coupon-related errors
 */
sealed class CouponException(message: String) : RuntimeException(message)

/**
 * Thrown when a coupon is not found
 */
class CouponNotFoundException(code: String) : 
    CouponException("Coupon not found with code: $code")

/**
 * Thrown when a coupon has expired
 */
class CouponExpiredException(code: String) : 
    CouponException("Coupon '$code' has expired")

/**
 * Thrown when coupon usage limit is exceeded
 */
class CouponUsageLimitExceededException(code: String) : 
    CouponException("Coupon '$code' has reached its usage limit")

/**
 * Thrown when a coupon is not applicable to a product
 */
class CouponNotApplicableException(code: String, reason: String) : 
    CouponException("Coupon '$code' is not applicable: $reason")

/**
 * Thrown when trying to create a coupon with duplicate code
 */
class DuplicateCouponCodeException(code: String) : 
    CouponException("Coupon code '$code' already exists")

/**
 * Thrown when coupon validation fails
 */
class InvalidCouponException(message: String) : 
    CouponException(message)

/**
 * Thrown when minimum order value is not met
 */
class MinimumOrderValueNotMetException(code: String, minValue: String) : 
    CouponException("Coupon '$code' requires minimum order value of $minValue")
