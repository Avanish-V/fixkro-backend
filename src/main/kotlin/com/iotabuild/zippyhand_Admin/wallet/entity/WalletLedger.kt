package com.iotabuild.zippyhand_Admin.wallet.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "wallet_ledger")
data class WalletLedger(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "technician_id", nullable = false)
    val technicianId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val type: TransactionType,

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 50)
    val entryType: EntryType,

    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,

    @Column(name = "reference_id")
    val referenceId: String?,

    @Column(name = "reference_type", length = 30)
    val referenceType: String?,

    @Column(length = 255)
    val description: String?,

    @Column(name = "idempotency_key", unique = true, length = 100)
    val idempotencyKey: String?,

    @Column(nullable = false, length = 20)
    val status: String = "SETTLED",

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

enum class TransactionType {
    CREDIT, DEBIT
}

enum class EntryType {
    JOB_EARNING, COMMISSION, WITHDRAWAL, BONUS, PENALTY, REFUND
}
