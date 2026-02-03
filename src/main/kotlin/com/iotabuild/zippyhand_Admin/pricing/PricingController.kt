package com.iotabuild.zippyhand_Admin.pricing

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * REST controller for pricing operations
 */
@RestController
@RequestMapping("/api/v1/pricing")
@Validated
class PricingController(private val pricingService: PricingService) {
    
    private val logger = LoggerFactory.getLogger(PricingController::class.java)
    
    /**
     * Calculate price for a product with optional coupon
     * 
     * @param request Price calculation request
     * @return Price breakdown including tax and discounts
     */
    @PostMapping("/calculate")
    fun calculatePrice(@Valid @RequestBody request: PriceRequest): ApiResponse<PriceDetailResponse> {
        logger.info("Received price calculation request for product: ${request.productId}")
        val priceDetails = pricingService.calculateFinalPrice(request)
        return ApiResponse.success(priceDetails)
    }
    
    /**
     * Exception handler for pricing-related errors
     */
    @ExceptionHandler(ProductNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProductNotFound(ex: ProductNotFoundException): ApiResponse<Nothing> {
        logger.error("Product not found: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Product not found", "PRODUCT_NOT_FOUND")
    }
    
    @ExceptionHandler(InvalidCouponException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidCoupon(ex: InvalidCouponException): ApiResponse<Nothing> {
        logger.error("Invalid coupon: ${ex.message}")
        return ApiResponse.error(ex.message ?: "Invalid coupon", "INVALID_COUPON")
    }
    
    @ExceptionHandler(PricingCalculationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleCalculationError(ex: PricingCalculationException): ApiResponse<Nothing> {
        logger.error("Pricing calculation error: ${ex.message}", ex)
        return ApiResponse.error("Failed to calculate price", "CALCULATION_ERROR")
    }
}
