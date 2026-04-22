package com.iotabuild.zippyhand_Admin.pricing.exception

import com.iotabuild.zippyhand_Admin.common.exception.ApiException
import org.springframework.http.HttpStatus

open class PricingException(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST) : ApiException(status, message, "PRICING_ERROR")

class ProductNotFoundException(productId: Long) : PricingException("Product not found: $productId", HttpStatus.NOT_FOUND)

class InvalidOfferException(message: String) : PricingException(message)

class PricingCalculationException(message: String, cause: Throwable? = null) : PricingException(message)
