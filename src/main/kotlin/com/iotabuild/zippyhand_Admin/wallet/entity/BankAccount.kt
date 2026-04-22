package com.iotabuild.zippyhand_Admin.wallet.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "bank_accounts")
data class BankAccount(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "technician_id", nullable = false, unique = true)
    val technicianId: String,

    @Column(name = "account_holder_name", nullable = false)
    val accountHolderName: String,

    @Column(name = "account_number", nullable = false)
    val accountNumber: String,

    @Column(name = "ifsc_code", nullable = false)
    val ifscCode: String,

    @Column(name = "bank_name")
    val bankName: String? = null,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean = false,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
