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

    @PostMapping("/verify")
    fun verifyPayment(@Valid @RequestBody request: VerifyPaymentRequest): ApiResponse<CashfreeOrderResponse> {
        return ApiResponse.success(paymentService.verifyPayment(request))
    }

    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("x-webhook-signature") signature: String?
    ): ApiResponse<String> {
        paymentService.handleWebhook(payload, signature)
        return ApiResponse.success("Webhook processed successfully")
    }
}
