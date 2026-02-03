package com.iotabuild.zippyhand_Admin.order

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(

    val id: Long = 0,
    val orderId: String,

    /* ---------- CUSTOMER (REFERENCE + SNAPSHOT) ---------- */

    val customerId: String,
    val serviceAddress: Address,

    /* ---------- SERVICE INFO ---------- */

    val productId:Long,
    val applianceBrand: String? = null,
    val applianceModel: String? = null,

    /* ---------- TECHNICIAN ---------- */

    val technicianId: Long? = null,

    /* ---------- PRICING ---------- */

    val price: Int,
    val tax: Int,
    val offerId: Long? = null,
    val discount: Int? = null,
    val totalAmount: Int,

    /* ---------- STATUS ---------- */

    val status: OrderStatus = OrderStatus.CREATED,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMode: PaymentMode? = null,

    /* ---------- TIMESTAMPS ---------- */

    val scheduledAt: String? = null,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val cancellationReason: String? = null

)
