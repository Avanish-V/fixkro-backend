package com.iotabuild.zippyhand_Admin.payment.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.payment.dto.*
import com.iotabuild.zippyhand_Admin.payment.service.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/create")
    fun createPaymentOrder(@Valid @RequestBody request: CreatePaymentOrderRequest): ApiResponse<PaymentOrderResponse> {
        return ApiResponse.success(paymentService.createPaymentOrder(request))
    }

    /**
     * Used by client apps to check the payment status of an order after payment sheet completes.
     * Returns the order status (e.g. PAID / ACTIVE / FAILED) without requiring manual verification.
     */
    @PostMapping("/verify")
    fun verifyPayment(@Valid @RequestBody request: VerifyPaymentRequest): ApiResponse<CashfreeOrderResponse> {
        return ApiResponse.success(paymentService.verifyPayment(request))
    }

    /**
     * Dedicated read-only check payment status endpoint.
     */
    @GetMapping("/check/{orderId}")
    fun checkPaymentStatus(@PathVariable orderId: String): ApiResponse<PaymentCheckStatusResponse> {
        return ApiResponse.success(paymentService.checkPaymentStatus(orderId))
    }

    /**
     * Retries payment for an existing order.
     * Reuses the existing Cashfree session when ACTIVE, or generates a new session for the same order if expired.
     */
    @PostMapping("/retry")
    fun retryPayment(@Valid @RequestBody request: RetryPaymentRequest): ApiResponse<PaymentOrderResponse> {
        return ApiResponse.success(paymentService.retryPayment(request.orderId))
    }

    /**
     * Automated webhook endpoint: marks payment status for BOTH orders and orders_payment
     * in a single atomic transaction.
     */
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader(value = "x-webhook-signature", required = false) signature: String?,
        @RequestHeader(value = "x-webhook-timestamp", required = false) timestamp: String?
    ): ApiResponse<String> {
        paymentService.handleWebhook(payload, signature, timestamp)
        return ApiResponse.success("Webhook processed successfully")
    }

    @GetMapping("/order/{orderId}")
    fun getPaymentByOrderId(@PathVariable orderId: String): ApiResponse<PaymentStatusResponse> {
        val payment = paymentService.getPaymentByOrderId(orderId)
            .orElseThrow { RuntimeException("Payment not found for order: $orderId") }
        return ApiResponse.success(paymentService.toPaymentStatusResponse(payment))
    }
}
