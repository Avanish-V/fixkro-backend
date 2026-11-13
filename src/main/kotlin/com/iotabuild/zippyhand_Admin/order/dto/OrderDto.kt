package com.iotabuild.zippyhand_Admin.order.dto

import com.iotabuild.zippyhand_Admin.order.entity.PaymentMode
import com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus
import com.iotabuild.zippyhand_Admin.order.entity.OrderStatus
import com.iotabuild.zippyhand_Admin.payment.dto.PaymentStatusResponse
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDateTime

data class OrderRequest(
    val productId: Long,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    @field:Max(value = 5, message = "Quantity cannot exceed 5")
    val quantity: Int = 1,
    val addressId: Long,
    val offerCode: String? = null,
    val paymentMode: PaymentMode? = PaymentMode.UPI,
    val serviceSchedule: String,
    val createdAt: String? = null
)

data class OrderResponse(
    val id: Long,
    val orderId: String,
    val customerId: String,
    val productId: Long,
    val productTitle: String,
    val productImage: String,
    val applianceBrand: String,
    val applianceModel: String,
    val technicianId: Long?,
    val technicianName: String?,
    val technicianPhoto: String?,
    val technicianUid: String?,
    val technicianPhone: String?,
    val otp: String?,
    val price: Int,
    val tax: Int,
    val offerId: Long?,
    val offerCode: String?,
    val discount: Int,
    val totalAmount: Int,
    val status: OrderStatus,
    val paymentStatus: PaymentStatus,
    val paymentMode: PaymentMode,
    val scheduledAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val cancellationReason: String?,
    val serviceAddress: Address,
    val paymentId: Long? = null,
    val payment: OrderPaymentResponse? = null,
    val paymentDetails: PaymentStatusResponse? = null
)

data class OrderPaymentResponse(
    val id: Long,
    val orderId: String,
    val cfOrderId: String?,
    val paymentSessionId: String?,
    val cfPaymentId: String?,
    val amount: Int,
    val currency: String,
    val paymentStatus: PaymentStatus,
    val paymentMode: PaymentMode,
    val customerName: String?,
    val customerPhone: String?,
    val customerEmail: String?,
    val bankReference: String?,
    val paymentMessage: String?,
    val paymentTime: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun com.iotabuild.zippyhand_Admin.order.entity.OrderPayment.toResponse(): OrderPaymentResponse {
    return OrderPaymentResponse(
        id = this.id,
        orderId = this.orderId,
        cfOrderId = this.cfOrderId,
        paymentSessionId = this.paymentSessionId,
        cfPaymentId = this.cfPaymentId,
        amount = this.amount,
        currency = this.currency,
        paymentStatus = this.paymentStatus,
        paymentMode = this.paymentMode,
        customerName = this.customerName,
        customerPhone = this.customerPhone,
        customerEmail = this.customerEmail,
        bankReference = this.bankReference,
        paymentMessage = this.paymentMessage,
        paymentTime = this.paymentTime,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class Address(
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
)

data class CreateOrderResponse(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val status: OrderStatus,
    val totalAmount: Int,
    val paymentId: Long? = null,
    val payment: OrderPaymentResponse? = null
)
