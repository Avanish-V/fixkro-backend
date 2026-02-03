package com.iotabuild.zippyhand_Admin.payment

import com.iotabuild.zippyhand_Admin.order.PaymentMode
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/* ---------- REQUEST DTOs ---------- */

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
    val paymentStatus: com.iotabuild.zippyhand_Admin.order.PaymentStatus
)

/* ---------- CASHFREE SPECIFIC DTOs ---------- */

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
    val order_meta: CashfreeOrderMeta?=null,
    val order_note: String? = null
)
