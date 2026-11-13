package com.iotabuild.zippyhand_Admin.payment.service

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "cashfree")
class CashfreeConfig {

    var appId: String = ""
    var secretKey: String = ""
    var environment: String = "sandbox"
    var apiVersion: String = "2023-08-01"
    /** Cashfree redirects the user here after payment completes.
     *  Use a deep-link (fixkro://payment/result) so the Android app intercepts it
     *  instead of opening a browser/WebView that shows a 404 page.
     *  Override via CASHFREE_RETURN_URL env var or cashfree.return-url in YAML. */
    var returnUrl: String = "fixkro://payment/result"
    /** Cashfree POSTs payment webhook events here — must be publicly reachable (use ngrok in dev) */
    var notifyUrl: String = ""

    fun getBaseUrl(): String {
        return if (environment.lowercase() in listOf("production", "prod")) {
            "https://api.cashfree.com/pg"
        } else {
            "https://sandbox.cashfree.com/pg"
        }
    }

    fun getPayoutBaseUrl(): String {
        return if (environment.lowercase() in listOf("production", "prod")) {
            "https://payout-api.cashfree.com"
        } else {
            "https://payout-gamma.cashfree.com"
        }
    }

    fun isProduction(): Boolean {
        return environment.lowercase() in listOf("production", "prod")
    }

    // 🔍 TEMP DEBUG (remove after testing)
    @PostConstruct
    fun debug() {
        println("==== CASHFREE CONFIG ====")
        println("APP_ID: $appId")
        println("SECRET_KEY: $secretKey")
        println("ENV: $environment")
        println("ACTIVE PROFILE: " + System.getenv("SPRING_PROFILES_ACTIVE"))
        println("=========================")
    }
}