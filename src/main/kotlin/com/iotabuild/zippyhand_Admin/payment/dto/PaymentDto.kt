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
    val currency: String = "INR"
)

data class PaymentStatusResponse(
    val orderId: String,
    val cfOrderId: String,
    val cfPaymentId: String?,
    val paymentStatus: com.iotabuild.zippyhand_Admin.payment.entity.PaymentStatus,
    val amount: Int,
    val currency: String,
    val paymentMode: String?,
    val paymentMessage: String?,
    val bankReference: String?,
    val paymentTime: java.time.LocalDateTime?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class PaymentVerificationResponse(
    val success: Boolean,
    val orderId: String,
    val paymentStatus: com.iotabuild.zippyhand_Admin.payment.entity.PaymentStatus,
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
