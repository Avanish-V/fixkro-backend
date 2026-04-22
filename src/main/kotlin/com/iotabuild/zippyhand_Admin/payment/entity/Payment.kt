package com.iotabuild.zippyhand_Admin.payment.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    val orderId: String,

    @Column(nullable = false, unique = true)
    val cfOrderId: String,

    @Column
    var cfPaymentId: String? = null,

    @Column(nullable = false)
    val customerName: String,

    @Column(nullable = false)
    val customerPhone: String,

    @Column(nullable = false)
    val customerEmail: String,

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

    @Column
    var bankReference: String? = null,

    @Column
    var paymentTime: LocalDateTime? = null,

    @Column
    var paymentGatewayDetails: String? = null,

    @Column(length = 500)
    val returnUrl: String,

    @Column(length = 1000)
    var webhookData: String? = null,

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
