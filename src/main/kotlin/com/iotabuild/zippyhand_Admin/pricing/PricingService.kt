package com.iotabuild.zippyhand_Admin.pricing

import com.iotabuild.zippyhand_Admin.coupon.service.CouponService
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for calculating product prices with tax and discounts
 */
@Service
class PricingService(
    private val productRepository: ProductRepository,
    private val pricingConfig: PricingConfig,
    private val couponService: CouponService
) {
    
    private val logger = LoggerFactory.getLogger(PricingService::class.java)
    
    /**
     * Calculate final price for a product including tax and discounts
     * 
     * @param request Price calculation request
     * @return Detailed price breakdown
     * @throws ProductNotFoundException if product doesn't exist
     * @throws InvalidCouponException if coupon is invalid
     */
    fun calculateFinalPrice(request: PriceRequest): PriceDetailResponse {
        logger.info("Calculating price for product ID: ${request.productId}, coupon: ${request.couponCode}")
        
        try {
            // Fetch product
            val product = productRepository.findById(request.productId)
                .orElseThrow { ProductNotFoundException(request.productId) }
            
            logger.debug("Found product: ${product.title}, base price: ${product.price}")
            
            // Convert unit price to BigDecimal and munltiply by quantity
            val unitPrice = BigDecimal(product.price)
            val basePrice = unitPrice.multiply(BigDecimal(request.quantity))
            
            // Calculate tax amount (tax rate is a percentage)
            val taxAmount = calculateTax(basePrice)
            
            // Apply coupon/discount if provided
            val discountAmount = applyCoupon(request.couponCode, product.productId, product.categoryId, basePrice)
            
            // Calculate final price: basePrice + tax - discount
            val finalPrice = (basePrice + taxAmount - discountAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO) // Ensure non-negative
            
            logger.info("Price calculation complete-${request.quantity} - Base: $basePrice, Tax: $taxAmount, Discount: $discountAmount, Final: $finalPrice")
            
            return PriceDetailResponse(
                basePrice = basePrice,
                taxAmount = taxAmount,
                discount = discountAmount,
                appliedCoupon = request.couponCode,
                totalPrice = finalPrice,
                currency = pricingConfig.currency
            )
            
        } catch (e: ProductNotFoundException) {
            logger.error("Product not found: ${request.productId}")
            throw e
        } catch (e: com.iotabuild.zippyhand_Admin.pricing.InvalidCouponException) {
            logger.error("Invalid coupon: ${request.couponCode}")
            throw e
        } catch (e: Exception) {
            logger.error("Error calculating price for product ${request.productId}", e)
            throw PricingCalculationException("Failed to calculate price", e)
        }
    }
    
    /**
     * Calculate tax amount based on configured tax rate
     * Formula: (basePrice * taxRate) / 100
     */
    private fun calculateTax(basePrice: BigDecimal): BigDecimal {
        return (basePrice * pricingConfig.taxRate / BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
    }
    
    /**
     * Apply coupon discount if valid
     */
    private fun applyCoupon(couponCode: String?, productId: Long, categoryId: Long, basePrice: BigDecimal): BigDecimal {
        if (couponCode.isNullOrBlank()) {
            return BigDecimal.ZERO
        }
        
        if (!pricingConfig.couponsEnabled) {
            logger.warn("Coupon system is disabled, ignoring coupon: $couponCode")
            return BigDecimal.ZERO
        }
        
        try {
            // Validate and calculate discount using CouponService
            val validationResult = couponService.validateAndCalculateDiscount(
                code = couponCode,
                productId = productId,
                categoryId = categoryId,
                orderAmount = basePrice
            )
            
            if (!validationResult.isValid) {
                logger.warn("Coupon validation failed: ${validationResult.errorMessage}")
                throw com.iotabuild.zippyhand_Admin.pricing.InvalidCouponException(
                    validationResult.errorMessage ?: "Invalid coupon"
                )
            }
            
            
            logger.info("Coupon applied successfully: $couponCode, discount: ${validationResult.calculatedDiscount}")
            return validationResult.calculatedDiscount
            
        } catch (e: com.iotabuild.zippyhand_Admin.pricing.InvalidCouponException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error applying coupon: $couponCode", e)
            throw com.iotabuild.zippyhand_Admin.pricing.InvalidCouponException("Failed to apply coupon: ${e.message}")
        }
    }
}
