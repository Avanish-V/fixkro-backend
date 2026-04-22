package com.iotabuild.zippyhand_Admin.offers.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "offers")
data class OfferEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "coupon_code", length = 50)
    val offerCode: String,

    @Column(name = "short_description")
    val shortDescription: String? = null,

    @Column(name = "category_id")
    val categoryId: Long? = null,

    @Column(name = "product_id")
    val productId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: OfferDiscountType,

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    val discountValue: BigDecimal,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDateTime,

    @Column(name = "usage_limit", nullable = false)
    val usageLimit: Int = 0, // 0 for unlimited

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    val userType: OfferUserType = OfferUserType.REGULAR,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OfferStatus = OfferStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OfferDiscountType {
    FIXED, PERCENTAGE
}

enum class OfferUserType {
    FIRST_USER, REGULAR
}

enum class OfferStatus {
    ACTIVE, INACTIVE
}
