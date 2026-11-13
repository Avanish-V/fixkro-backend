package com.iotabuild.zippyhand_Admin.payment.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments", indexes = [
    Index(name = "idx_payments_order_id", columnList = "orderId"),
    Index(name = "idx_payments_cf_order_id", columnList = "cfOrderId"),
    Index(name = "idx_payments_status", columnList = "paymentStatus")
])
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var orderId: String = "",

    @Column(nullable = true, unique = true)
    var cfOrderId: String? = null,

    @Column
    var cfPaymentId: String? = null,

    @Column
    var paymentSessionId: String? = null,

    @Column(nullable = false)
    var customerName: String = "",

    @Column(nullable = false)
    var customerPhone: String = "",

    @Column(nullable = false)
    var customerEmail: String = "",

    @Column(nullable = false)
    var amount: Int = 0,

    @Column(nullable = false)
    var currency: String = "INR",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Column
    var paymentMode: String? = null,

    @Column
    var paymentMessage: String? = null,

    @Column
    var bankReference: String? = null,

    @Column
    var paymentTime: LocalDateTime? = null,

    @Column
    var paymentGatewayDetails: String? = null,

    @Column(length = 500)
    var returnUrl: String? = null,

    @Column(length = 1000)
    var webhookData: String? = null,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

enum class PaymentStatus {
    PENDING,
    ACTIVE,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED,
    PARTIAL
}
