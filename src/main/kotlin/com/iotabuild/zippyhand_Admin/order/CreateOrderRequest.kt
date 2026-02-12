package com.iotabuild.zippyhand_Admin.order

import jakarta.persistence.Column
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime


data class OrderRequest(
    val serviceAddress: Address,
    val serviceProduct: ServiceProduct,
    val technicianId: Int? = null,
    val priceDetail: PriceDetail,
    val transaction: Transaction,
    val createdAt: String,
    val serviceSchedule: String
)


data class ServiceProduct(
    val productId: Int,
    val productTitle: String,
    val applianceBrand: String,
    val applianceModel: String,
)

data class PriceDetail(
    val price: Int,
    val tax: Int,
    val coupon: String,
    val discount: Int,
    val totalAmount: Int
)


data class Transaction(
    val transactionId: String,
    val paymentMode: String,
    val paymentStatus: String
)


data class Address(
    val type: String = "", // Home, Work, etc.
    val id: Long? = null,
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val postalCode: String,
    val isDefault: Boolean = false
)
