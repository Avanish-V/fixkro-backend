package com.iotabuild.zippyhand_Admin.payment

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "cashfree")
class CashfreeProperties {
    var appId: String = "257493c08aee5e707d618168a4394752"
    var secretKey: String = "cd1d129bca05453908452ffff5419de176aed9f7"
    var apiVersion: String = "2025-01-01"
    var environment: String = "TEST"
    var baseUrl: String = "https://sandbox.cashfree.com/pg"

    @PostConstruct
    fun validate() {
        if (appId.isBlank() || appId == "your-app-id") {
            logger.warn("Cashfree App ID is not configured. Payment functionality will fail. Set CASHFREE_APP_ID environment variable.")
        }
        if (secretKey.isBlank() || secretKey == "your-secret-key") {
            logger.warn("Cashfree Secret Key is not configured. Payment functionality will fail. Set CASHFREE_SECRET_KEY environment variable.")
        }
        logger.info("Cashfree configuration loaded: Environment=$environment, BaseURL=$baseUrl")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CashfreeProperties::class.java)
    }
}

@Configuration
class CashfreeConfig(private val properties: CashfreeProperties) {

    fun getAppId(): String = properties.appId

    fun getSecretKey(): String = properties.secretKey

    fun getApiVersion(): String = properties.apiVersion

    fun getBaseUrl(): String = properties.baseUrl

    fun getEnvironment(): String = properties.environment

    fun isProduction(): Boolean = properties.environment.equals("PRODUCTION", ignoreCase = true)
}
