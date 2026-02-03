package com.iotabuild.zippyhand_Admin.payment

import java.time.LocalDateTime

/* ---------- RESPONSE DTOs ---------- */

data class PaymentOrderResponse(
    //val success: Boolean,
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val orderAmount: Int,
    val currency: String = "INR",
    //val status: PaymentStatus
)

data class PaymentStatusResponse(
    val orderId: String,
    val cfOrderId: String,
    val cfPaymentId: String?,
    val paymentStatus: PaymentStatus,
    val amount: Int,
    val currency: String,
    val paymentMode: String?,
    val paymentMessage: String?,
    val bankReference: String?,
    val paymentTime: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class PaymentVerificationResponse(
    val success: Boolean,
    val orderId: String,
    val paymentStatus: PaymentStatus,
    val message: String,
    val paymentDetails: PaymentStatusResponse?
)

/* ---------- CASHFREE API RESPONSE DTOs ---------- */

data class CashfreeOrderResponse(
    val cf_order_id: String,
    val order_id: String,
    val entity: String,
    val order_currency: String,
    val order_amount: Double,
    val order_status: String,
    val payment_session_id: String,
    val order_token: String? = null
)

data class CashfreePaymentResponse(
    val cf_payment_id: String,
    val order_id: String,
    val entity: String,
    val payment_currency: String,
    val payment_amount: Double,
    val payment_time: String?,
    val payment_status: String,
    val payment_method: PaymentMethodDetails?,
    val bank_reference: String?,
    val payment_message: String?
)

data class PaymentMethodDetails(
    val payment_method: String? = null
)

data class CashfreeWebhookData(
    val order: WebhookOrder?,
    val payment: WebhookPayment?
)

data class WebhookOrder(
    val order_id: String,
    val order_amount: Double,
    val order_currency: String,
    val order_status: String
)

data class WebhookPayment(
    val cf_payment_id: String,
    val payment_status: String,
    val payment_amount: Double,
    val payment_currency: String,
    val payment_message: String?,
    val payment_time: String?,
    val bank_reference: String?,
    val payment_method: Map<String, Any>?
)
