package com.iotabuild.zippyhand_Admin.pricing.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

/**
 * Configuration for pricing calculation
 */
@Configuration
@ConfigurationProperties(prefix = "zippyhand.pricing")
class PricingConfig {
    /**
     * Standard tax rate percentage (e.g. 18.0)
     */
    var taxRate: BigDecimal = BigDecimal("5.0")
    
    /**
     * Default currency code
     */
    var currency: String = "INR"
    
    /**
     * Whether offer discounts are enabled
     */
    var offersEnabled: Boolean = true
}
