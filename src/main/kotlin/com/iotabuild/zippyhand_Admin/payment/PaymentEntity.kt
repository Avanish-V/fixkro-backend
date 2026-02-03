package com.iotabuild.zippyhand_Admin.payment

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    /* ---------- ORDER REFERENCE ---------- */

    @Column(nullable = false, unique = true)
    val orderId: String,

    /* ---------- CASHFREE IDS ---------- */

    @Column(nullable = false, unique = true)
    val cfOrderId: String,

    @Column
    var cfPaymentId: String? = null,

    /* ---------- CUSTOMER INFO ---------- */

    @Column(nullable = false)
    val customerName: String,

    @Column(nullable = false)
    val customerPhone: String,

    @Column(nullable = false)
    val customerEmail: String,

    /* ---------- PAYMENT DETAILS ---------- */

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val currency: String = "INR",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Column
    var paymentMode: String? = null,

    @Column
    var paymentMessage: String? = null,

    /* ---------- TRANSACTION INFO ---------- */

    @Column
    var bankReference: String? = null,

    @Column
    var paymentTime: LocalDateTime? = null,

    @Column
    var paymentGatewayDetails: String? = null,

    /* ---------- METADATA ---------- */

    @Column(length = 500)
    val returnUrl: String,

    @Column(length = 1000)
    var webhookData: String? = null,

    /* ---------- TIMESTAMPS ---------- */

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING,
    ACTIVE,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED,
    PARTIAL
}
