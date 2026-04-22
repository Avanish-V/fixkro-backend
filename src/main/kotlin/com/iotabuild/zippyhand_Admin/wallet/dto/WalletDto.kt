package com.iotabuild.zippyhand_Admin.wallet.dto

import com.iotabuild.zippyhand_Admin.wallet.entity.WalletLedger
import java.math.BigDecimal
import java.time.LocalDateTime

data class WalletDashboardResponse(
    val balance: BigDecimal,
    val totalEarnings: BigDecimal,
    val recentTransactions: List<WalletLedger>
)

data class BankAccountUpdateRequest(
    val accountHolderName: String,
    val accountNumber: String,
    val confirmAccountNumber: String,
    val ifscCode: String,
    val bankName: String? = null
) {
    fun isValid(): Boolean = accountNumber == confirmAccountNumber && accountNumber.length >= 9
}

data class BankAccountResponse(
    val accountHolderName: String,
    val maskedAccountNumber: String,
    val ifscCode: String,
    val bankName: String?,
    val isVerified: Boolean,
    val updatedAt: LocalDateTime
)
