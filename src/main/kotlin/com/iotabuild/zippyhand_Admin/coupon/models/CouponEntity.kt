package com.iotabuild.zippyhand_Admin.coupon.models

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Coupon entity for discount management
 */
@Entity
@Table(name = "coupons")
data class CouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val couponId: Long = 0,
    
    @Column(unique = true, nullable = false, length = 50)
    val code: String = "",
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val discountValue: BigDecimal = BigDecimal.ZERO,
    
    @Column(precision = 10, scale = 2)
    val minOrderValue: BigDecimal? = null,
    
    @Column(precision = 10, scale = 2)
    val maxDiscountAmount: BigDecimal? = null,
    
    @Column(nullable = false)
    val validFrom: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val validUntil: LocalDateTime = LocalDateTime.now().plusDays(30),
    
    val totalUsageLimit: Int? = null,
    
    @Column(nullable = false)
    val perUserUsageLimit: Int = 1,
    
    @Column(nullable = false)
    val currentUsageCount: Int = 0,
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val applicableProductIds: List<Long>? = null,
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val applicableCategoryIds: List<Long>? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Discount type enumeration
 */
enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}
