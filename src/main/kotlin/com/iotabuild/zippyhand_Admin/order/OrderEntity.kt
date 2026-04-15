package com.iotabuild.zippyhand_Admin.order

import jakarta.persistence.Entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders", indexes = [
    Index(name = "idx_customer_id", columnList = "customerId"),
    Index(name = "idx_order_id", columnList = "orderId", unique = true),
    Index(name = "idx_status", columnList = "status")
])
class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    val orderId: String,

    /* ---------- CUSTOMER (REFERENCE + SNAPSHOT) ---------- */

    @Column(nullable = false)
    val customerId: String,

    /* ---------- ADDRESS ---------- */
    @Column(nullable = false)
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val postalCode: String,

    /* ---------- SERVICE INFO ---------- */

    @Column(nullable = false)
    val productId: Long,
    val productTitle: String,
    val productImage: String,
    val applianceBrand: String? = null,
    val applianceModel: String? = null,

    /* ---------- PRICING ---------- */

    @Column(nullable = false)
    val price: Int,
    @Column(nullable = false)
    val tax: Int,
    val offerId: Long? = null,
    val couponCode: String? = null,
    @Column(nullable = false)
    val discount: Int? = null,
    @Column(nullable = false)
    val totalAmount: Int,

    /* ---------- STATUS ---------- */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.CREATED,

    /* ---------- PAYMENT INFO ---------- */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    val paymentMode: PaymentMode = PaymentMode.NET_BANKING,

    /* ---------- TIMESTAMPS ---------- */

    val scheduledAt: String? = null,
    var completedAt: LocalDateTime? = null,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var cancellationReason: String? = null

) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun canTransitionTo(newStatus: OrderStatus): Boolean {
        return when (this.status) {
            OrderStatus.CREATED -> newStatus in listOf(OrderStatus.ASSIGNED, OrderStatus.CANCELLED)
            OrderStatus.ASSIGNED -> newStatus in listOf(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED)
            OrderStatus.IN_PROGRESS -> newStatus in listOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED)
            OrderStatus.COMPLETED -> false
            OrderStatus.CANCELLED -> false
        }
    }
}


enum class OrderStatus {
    CREATED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

enum class PaymentMode {
    CASH,
    UPI,
    CARD,
    NET_BANKING
}

enum class ServiceType {
    AC_REPAIR,
    AC_SERVICE,
    WASHING_MACHINE_REPAIR,
    FRIDGE_REPAIR,
    RO_SERVICE
}

