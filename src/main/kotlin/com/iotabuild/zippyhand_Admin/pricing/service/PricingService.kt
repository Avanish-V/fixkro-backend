package com.iotabuild.zippyhand_Admin.pricing.service

import com.iotabuild.zippyhand_Admin.offers.service.OfferQueryService
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceDetailResponse
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.exception.InvalidOfferException
import com.iotabuild.zippyhand_Admin.pricing.exception.PricingCalculationException
import com.iotabuild.zippyhand_Admin.pricing.exception.ProductNotFoundException
import com.iotabuild.zippyhand_Admin.products.service.ProductQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for calculating product prices with tax and discounts
 */
@Service
class PricingService(
    private val productQueryService: ProductQueryService,
    private val pricingConfig: PricingConfig,
    private val offerQueryService: OfferQueryService
) {
    
    private val logger = LoggerFactory.getLogger(PricingService::class.java)
    
    /**
     * Calculate final price for a product including tax and discounts
     */
    fun calculateFinalPrice(request: PriceRequest): PriceDetailResponse {
        if (request.quantity > 5) {
            logger.warn("Received excessive quantity: {}. Restricting to maximum of 5.", request.quantity)
        }
        val sanitizedQuantity = request.quantity.coerceIn(1, 5)
        if (request.quantity <= 0) {
            logger.warn("Received invalid quantity: {}. Coercing to 1 for calculation context.", request.quantity)
        }

        logger.info("Starting price calculation: productId={}, quantity={}, offer={}", 
            request.productId, sanitizedQuantity, request.offerCode ?: "NONE")
        
        try {
            val product = try {
                productQueryService.getProduct(request.productId)
            } catch (e: NoSuchElementException) {
                throw ProductNotFoundException(request.productId)
            }
            
            val unitPrice = BigDecimal(product.price.toString())
            val basePrice = unitPrice.multiply(BigDecimal(sanitizedQuantity))
            
            val taxAmount = calculateTax(basePrice)
            val discountAmount = applyOffer(request.offerCode, product.productId, product.categoryId, basePrice)
            
            val finalPrice = basePrice.add(taxAmount).subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP)
            
            if (finalPrice < BigDecimal.ZERO) {
                logger.error("Price resulted in negative value: finalPrice={}, base={}, tax={}, discount={}", 
                    finalPrice, basePrice, taxAmount, discountAmount)
                throw PricingCalculationException("Final price cannot be negative. Please check applied discounts.")
            }
            
            logger.info("Successfully calculated price: base={}, tax={}, discount={}, total={}", 
                basePrice, taxAmount, discountAmount, finalPrice)

            return PriceDetailResponse(
                basePrice = basePrice,
                taxAmount = taxAmount,
                discount = discountAmount,
                appliedOffer = request.offerCode,
                totalPrice = finalPrice,
                currency = pricingConfig.currency
            )
            
        } catch (e: ProductNotFoundException) {
            throw e
        } catch (e: InvalidOfferException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error calculating price", e)
            throw PricingCalculationException("Failed to calculate price", e)
        }
    }
    
    private fun calculateTax(basePrice: BigDecimal): BigDecimal {
        val rate = pricingConfig.taxRate
        return basePrice.multiply(rate)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
    }
    
    private fun applyOffer(offerCode: String?, productId: Long, categoryId: Long, basePrice: BigDecimal): BigDecimal {
        if (offerCode.isNullOrBlank() || !pricingConfig.offersEnabled) {
            return BigDecimal.ZERO
        }
        
        try {
            val validationResult = offerQueryService.validateAndCalculateOffer(
                code = offerCode,
                productId = productId,
                categoryId = categoryId,
                orderAmount = basePrice
            )
            
            if (!validationResult.isValid) {
                throw InvalidOfferException(validationResult.errorMessage ?: "Invalid offer")
            }
            
            return validationResult.calculatedDiscount
            
        } catch (e: InvalidOfferException) {
            throw e
        } catch (e: Exception) {
            throw InvalidOfferException("Failed to apply offer: ${e.message}")
        }
    }
}
