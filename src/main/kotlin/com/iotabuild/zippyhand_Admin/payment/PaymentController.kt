package com.iotabuild.zippyhand_Admin.payment

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    /**
     * Create a Cashfree payment order
     * POST /api/v1/payments/create-order
     */
    @PostMapping("/create-session")
    fun createPaymentOrder(
        @Valid @RequestBody request: CreatePaymentOrderRequest
    ): ResponseEntity<ApiResponse<PaymentOrderResponse>> {

        val response = paymentService.createPaymentOrder(request)
        
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Verify payment status
     * POST /api/v1/payments/verify
     */
    @PostMapping("/verify")
    fun verifyPayment(
        @Valid @RequestBody request: VerifyPaymentRequest
    ): ResponseEntity<ApiResponse<CashfreeOrderResponse>> {
        logger.info("Verifying payment for order ID: ${request.orderId}")
        
        val response = paymentService.verifyPayment(request)
        
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get payment status
     * GET /api/v1/payments/status/{orderId}
     */
//    @GetMapping("/status/{orderId}")
//    fun getPaymentStatus(
//        @PathVariable orderId: Long
//    ): ResponseEntity<ApiResponse<PaymentStatusResponse>> {
//        logger.info("Getting payment status for order ID: $orderId")
//
//        val response = paymentService.getPaymentStatus(orderId)
//
//        return ResponseEntity.ok(ApiResponse.success(response))
//    }

    /**
     * Cashfree webhook endpoint
     * POST /api/v1/payments/webhook
     * This endpoint is publicly accessible (no authentication required)
     */
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody webhookPayload: String,
        @RequestHeader("x-webhook-signature", required = false) signature: String?
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Received Cashfree webhook")
        
        return try {
            val processed = paymentService.handleWebhook(webhookPayload, signature)
            
            if (processed) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "message" to "Webhook processed successfully"
                ))
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                    "success" to false,
                    "message" to "Failed to process webhook"
                ))
            }
        } catch (e: InvalidWebhookSignatureException) {
            logger.warn("Invalid webhook signature")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf(
                "success" to false,
                "message" to "Invalid signature"
            ))
        } catch (e: Exception) {
            logger.error("Webhook processing error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Internal error processing webhook"
            ))
        }
    }

    /**
     * Health check endpoint for payment service
     * GET /api/v1/payments/health
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "Payment Service",
            "timestamp" to java.time.LocalDateTime.now().toString()
        ))
    }
}
