package com.iotabuild.zippyhand_Admin.wallet.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "withdrawal_requests")
data class WithdrawalRequest(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "technician_id", nullable = false)
    val technicianId: String,

    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: WithdrawalStatus = WithdrawalStatus.PENDING,

    @Column(name = "bank_account_id", nullable = false)
    val bankAccountId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "processed_at")
    var processedAt: OffsetDateTime? = null,

    @Column(length = 255)
    var remarks: String? = null,

    @Column(name = "idempotency_key", unique = true, length = 100)
    val idempotencyKey: String? = null
)

enum class WithdrawalStatus {
    PENDING, APPROVED, REJECTED, COMPLETED
}
