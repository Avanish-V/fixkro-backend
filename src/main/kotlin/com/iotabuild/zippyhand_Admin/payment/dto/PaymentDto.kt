package com.iotabuild.zippyhand_Admin.payment.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreatePaymentOrderRequest(
    @field:NotBlank(message = "Customer name is required")
    val customerName: String,

    @field:NotBlank(message = "Customer phone is required")
    val customerPhone: String,

    @field:NotBlank(message = "Customer email is required")
    @field:Email(message = "Invalid email format")
    val customerEmail: String,

    @field:NotNull(message = "Product is required")
    val productId: Long,

    val orderNote: String? = null
)

data class VerifyPaymentRequest(
    @field:NotNull(message = "Order ID is required")
    val orderId: String
)

data class RetryPaymentRequest(
    @field:NotBlank(message = "Order ID is required")
    val orderId: String
)

data class UpdatePaymentStatusRequest(
    @field:NotNull(message = "Order ID is required")
    val orderId: Long,

    @field:NotNull(message = "Payment status is required")
    val paymentStatus: com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus
)

data class CashfreeCustomer(
    val customer_id: String,
    val customer_name: String,
    val customer_email: String,
    val customer_phone: String
)

data class CashfreeOrderMeta(
    val return_url: String,
    val notify_url: String? = null,
    val payment_methods: String? = null
)

data class CashfreeCreateOrderRequest(
    val order_id: String,
    val order_amount: Double,
    val order_currency: String = "INR",
    val customer_details: CashfreeCustomer,
    val order_meta: CashfreeOrderMeta? = null,
    val order_note: String? = null
)

/* ---------- RESPONSE DTOs ---------- */

data class PaymentOrderResponse(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val orderAmount: Int,
    val currency: String = "INR",
    val paymentId: Long? = null
)

data class PaymentStatusResponse(
    val id: Long? = null,
    val orderId: String,
    val cfOrderId: String? = null,
    val cfPaymentId: String? = null,
    val paymentSessionId: String? = null,
    val paymentStatus: com.iotabuild.zippyhand_Admin.payment.entity.PaymentStatus,
    val amount: Int,
    val currency: String = "INR",
    val paymentMode: String? = null,
    val paymentMessage: String? = null,
    val bankReference: String? = null,
    val paymentTime: java.time.LocalDateTime? = null,
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

fun com.iotabuild.zippyhand_Admin.payment.entity.Payment.toStatusResponse(): PaymentStatusResponse {
    return PaymentStatusResponse(
        id = this.id,
        orderId = this.orderId,
        cfOrderId = this.cfOrderId,
        cfPaymentId = this.cfPaymentId,
        paymentSessionId = this.paymentSessionId,
        paymentStatus = this.paymentStatus,
        amount = this.amount,
        currency = this.currency,
        paymentMode = this.paymentMode,
        paymentMessage = this.paymentMessage,
        bankReference = this.bankReference,
        paymentTime = this.paymentTime,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class PaymentVerificationResponse(
    val success: Boolean,
    val orderId: String,
    val paymentStatus: com.iotabuild.zippyhand_Admin.payment.entity.PaymentStatus,
    val message: String,
    val paymentDetails: PaymentStatusResponse?
)

/* ---------- CASHFREE API RESPONSE DTOs ---------- */

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class CashfreeOrderResponse(
    val cf_order_id: Any? = null,
    val order_id: String = "",
    val entity: String = "order",
    val order_currency: String = "INR",
    val order_amount: Double = 0.0,
    val order_status: String = "",
    val payment_session_id: String = "",
    val order_token: String? = null
) {
    fun getCfOrderIdStr(): String = cf_order_id?.toString() ?: ""
}

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class CashfreePaymentResponse(
    val cf_payment_id: Any? = null,
    val order_id: String = "",
    val entity: String = "payment",
    val payment_currency: String = "INR",
    val payment_amount: Double = 0.0,
    val payment_time: String? = null,
    val payment_status: String = "",
    val payment_method: Any? = null,
    val bank_reference: String? = null,
    val payment_message: String? = null,
    val payment_group: String? = null
) {
    fun getCfPaymentIdStr(): String? = cf_payment_id?.toString()
}

data class PaymentMethodDetails(
    val payment_method: String? = null
)

data class PaymentCheckStatusResponse(
    val orderId: String,
    val cfOrderId: String?,
    val isPaid: Boolean,
    val paymentStatus: com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus,
    val orderStatus: com.iotabuild.zippyhand_Admin.order.entity.OrderStatus,
    val amount: Int,
    val paymentMode: com.iotabuild.zippyhand_Admin.order.entity.PaymentMode?,
    val cfPaymentId: String?,
    val bankReference: String?,
    val paymentTime: java.time.LocalDateTime?,
    val message: String
)

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class CashfreeWebhookRoot(
    val data: CashfreeWebhookData? = null,
    val order: WebhookOrder? = null,
    val payment: WebhookPayment? = null,
    val type: String? = null,
    val event_time: String? = null
) {
    /** Resolves order from nested data block (modern) or top-level (legacy) */
    fun resolveOrder(): WebhookOrder? = data?.order ?: order
    /** Resolves payment from nested data block (modern) or top-level (legacy) */
    fun resolvePayment(): WebhookPayment? = data?.payment ?: payment
}

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class CashfreeWebhookData(
    val order: WebhookOrder? = null,
    val payment: WebhookPayment? = null,
    val customer_details: Map<String, Any>? = null
)

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookOrder(
    val order_id: String = "",
    val order_amount: Double = 0.0,
    val order_currency: String = "INR",
    val order_status: String? = null
)

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookPayment(
    val cf_payment_id: String? = null,
    val payment_status: String = "",
    val payment_amount: Double = 0.0,
    val payment_currency: String = "INR",
    val payment_message: String? = null,
    val payment_time: String? = null,
    val bank_reference: String? = null,
    val payment_group: String? = null,
    val payment_method: Any? = null
)
