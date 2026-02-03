package com.iotabuild.zippyhand_Admin.pricing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

/**
 * Configuration properties for pricing service
 */
@Configuration
@ConfigurationProperties(prefix = "pricing")
class
PricingConfig {
    
    /**
     * Tax rate as percentage (e.g., 18.0 for 18%)
     */
    var taxRate: BigDecimal = BigDecimal("18.0")
    
    /**
     * Currency code (ISO 4217)
     */
    var currency: String = "INR"
    
    /**
     * Enable/disable coupon functionality
     */
    var couponsEnabled: Boolean = false
}
