package com.iotabuild.zippyhand_Admin.offers.dto

import com.iotabuild.zippyhand_Admin.offers.entity.OfferDiscountType
import com.iotabuild.zippyhand_Admin.offers.entity.OfferStatus
import com.iotabuild.zippyhand_Admin.offers.entity.OfferUserType
import java.math.BigDecimal
import java.time.LocalDateTime

data class OfferResponse(
    val id: Long,
    val offerCode: String?,
    val shortDescription: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val productId: Long?,
    val productName: String?,
    val discountType: OfferDiscountType,
    val discountValue: BigDecimal,
    val expiryDate: LocalDateTime,
    val limit: Int,
    val userType: OfferUserType,
    val status: OfferStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateOfferRequest(
    val offerCode: String,
    val shortDescription: String?,
    val categoryId: Long?,
    val productId: Long?,
    val discountType: OfferDiscountType,
    val discountValue: BigDecimal,
    val expiryDate: LocalDateTime,
    val limit: Int,
    val userType: OfferUserType
)

data class OfferValidationResponse(
    val isValid: Boolean,
    val calculatedDiscount: BigDecimal = BigDecimal.ZERO,
    val errorMessage: String? = null
)
