package com.iotabuild.zippyhand_Admin.order

import jakarta.persistence.Entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    val orderId: String,

    /* ---------- CUSTOMER (REFERENCE + SNAPSHOT) ---------- */

    @Column(nullable = false)
    val customerId: String,

    /* ---------- ADDRESS ---------- */
    @Column(nullable = false)
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val city: String,
    val state: String,
    val postalCode: String,

    /* ---------- SERVICE INFO ---------- */

    @Column(nullable = false)
    val productId:Long,
    val applianceBrand: String? = null,
    val applianceModel: String? = null,

    /* ---------- TECHNICIAN ---------- */

    val technicianId: Long? = null,

    /* ---------- PRICING ---------- */

    @Column(nullable = false)
    val price: Int,
    @Column(nullable = false)
    val tax: Int,
    val offerId: Long? = null,
    @Column(nullable = false)
    val discount: Int?= null,
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
    val completedAt: LocalDateTime? = null,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now(),

    val cancellationReason: String? = null

)


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

