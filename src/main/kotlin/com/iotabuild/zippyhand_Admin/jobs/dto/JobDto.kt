package com.iotabuild.zippyhand_Admin.jobs.dto

import java.time.LocalDateTime

data class JobListItemResponse(
    val id: Long?,
    val orderId: String,
    val partnerId: String,
    val status: String,
    val assignedAt: LocalDateTime,
    val acceptedAt: LocalDateTime?,
    val productTitle: String?,
    val scheduledAt: LocalDateTime?,
    val addressLine1: String?,
    val city: String?,
    val customerName: String?,
    val customerPhone: String?,
    val totalPrice: Int?
)

data class JobDetailsResponse(
    val id: Long?,
    val orderId: String,
    val partnerId: String,
    val status: String,
    val assignedAt: LocalDateTime,
    val acceptedAt: LocalDateTime?,
    val customerName: String?,
    val customerPhone: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val state: String?,
    val postalCode: String?,
    val applianceBrand: String?,
    val applianceModel: String?,
    val productTitle: String?,
    val scheduledAt: LocalDateTime?,
    val price: Int?,
    val tax: Int?,
    val discount: Int?,
    val totalPrice: Int?,
    val commission: Int?,
    val payout: Int?
)
