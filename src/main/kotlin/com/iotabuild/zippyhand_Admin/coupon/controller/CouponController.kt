package com.iotabuild.zippyhand_Admin.coupon.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.coupon.*
import com.iotabuild.zippyhand_Admin.coupon.models.*
import com.iotabuild.zippyhand_Admin.coupon.service.CouponService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * REST controller for coupon management
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Validated
class CouponController(
    private val couponService: CouponService
) {
    
    private val logger = LoggerFactory.getLogger(CouponController::class.java)
    
    /**
     * Create a new coupon
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCoupon(@Valid @RequestBody request: CreateCouponRequest): ApiResponse<CouponResponse> {
        logger.info("Received request to create coupon: ${request.code}")
        val coupon = couponService.createCoupon(request)
        return ApiResponse.success(coupon)
    }
    
    /**
     * Get coupon by code
     */
    @GetMapping("/{code}")
    fun getCoupon(@PathVariable code: String): ApiResponse<CouponResponse> {
        logger.info("Received request to get coupon: $code")
        val coupon = couponService.getCouponByCode(code)
        return ApiResponse.success(coupon)
    }
    
    /**
     * Get all coupons
     */
    @GetMapping
    fun getAllCoupons(
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean
    ): ApiResponse<List<CouponSummaryResponse>> {
        logger.info("Received request to list coupons (activeOnly: $activeOnly)")
        val coupons = if (activeOnly) {
            couponService.getActiveCoupons()
        } else {
            couponService.getAllCoupons()
        }
        return ApiResponse.success(coupons)
    }
    
    /**
     * Update coupon
     */
    @PutMapping("/{code}")
    fun updateCoupon(
        @PathVariable code: String,
        @Valid @RequestBody request: UpdateCouponRequest
    ): ApiResponse<CouponResponse> {
        logger.info("Received request to update coupon: $code")
        val coupon = couponService.updateCoupon(code, request)
        return ApiResponse.success(coupon)
    }
    
    /**
     * Deactivate coupon
     */
    @DeleteMapping("/{code}")
    fun deactivateCoupon(@PathVariable code: String): ApiResponse<CouponResponse> {
        logger.info("Received request to deactivate coupon: $code")
        val coupon = couponService.deactivateCoupon(code)
        return ApiResponse.success(coupon)
    }
    
    /**
     * Exception handlers
     */
    @ExceptionHandler(CouponNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCouponNotFound(ex: CouponNotFoundException): ApiResponse<Nothing> {
        logger.error("Coupon not found: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Coupon not found", "COUPON_NOT_FOUND")
    }
    
    @ExceptionHandler(DuplicateCouponCodeException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateCouponCode(ex: DuplicateCouponCodeException): ApiResponse<Nothing> {
        logger.error("Duplicate coupon code: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Duplicate coupon code", "DUPLICATE_COUPON_CODE")
    }
    
    @ExceptionHandler(InvalidCouponException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidCoupon(ex: InvalidCouponException): ApiResponse<Nothing> {
        logger.error("Invalid coupon: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Invalid coupon", "INVALID_COUPON")
    }
    
    @ExceptionHandler(CouponExpiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCouponExpired(ex: CouponExpiredException): ApiResponse<Nothing> {
        logger.error("Coupon expired: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Coupon expired", "COUPON_EXPIRED")
    }
    
    @ExceptionHandler(CouponUsageLimitExceededException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUsageLimitExceeded(ex: CouponUsageLimitExceededException): ApiResponse<Nothing> {
        logger.error("Usage limit exceeded: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Usage limit exceeded", "USAGE_LIMIT_EXCEEDED")
    }
    
    @ExceptionHandler(CouponNotApplicableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCouponNotApplicable(ex: CouponNotApplicableException): ApiResponse<Nothing> {
        logger.error("Coupon not applicable: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Coupon not applicable", "COUPON_NOT_APPLICABLE")
    }
}
