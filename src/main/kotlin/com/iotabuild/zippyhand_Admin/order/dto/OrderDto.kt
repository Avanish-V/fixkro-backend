package com.iotabuild.zippyhand_Admin.order.dto

import com.iotabuild.zippyhand_Admin.order.entity.PaymentMode
import com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus
import com.iotabuild.zippyhand_Admin.order.entity.OrderStatus
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
    val serviceAddress: Address
)

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
    val totalAmount: Int
)
