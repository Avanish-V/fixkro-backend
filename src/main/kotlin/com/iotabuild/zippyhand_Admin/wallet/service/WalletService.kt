package com.iotabuild.zippyhand_Admin.wallet.service

import com.iotabuild.zippyhand_Admin.wallet.dto.WalletDashboardResponse
import com.iotabuild.zippyhand_Admin.wallet.entity.*
import com.iotabuild.zippyhand_Admin.wallet.repository.WalletLedgerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import org.slf4j.LoggerFactory

@Service
class WalletService(private val repository: WalletLedgerRepository) {
    private val logger = LoggerFactory.getLogger(WalletService::class.java)

    @Transactional
    fun recordJobEarning(
        technicianUid: String,
        orderId: String,
        totalAmount: BigDecimal,
        commissionAmount: BigDecimal
    ) {
        val payoutAmount = totalAmount.subtract(commissionAmount)
        val idempotencyKey = "job_completion_$orderId"

        if (repository.findByIdempotencyKey(idempotencyKey) != null) {
            logger.warn("Wallet record for order $orderId already exists. Skipping duplicate credit.")
            return
        }

        val masterEntry = WalletLedger(
            technicianId = technicianUid,
            type = TransactionType.CREDIT,
            entryType = EntryType.JOB_EARNING,
            amount = payoutAmount,
            referenceId = orderId,
            referenceType = "ORDER",
            description = "Net payout for order $orderId (Total: $totalAmount, Commission: $commissionAmount)",
            idempotencyKey = idempotencyKey
        )

        repository.save(masterEntry)
        logger.info("Successfully recorded wallet payout for technician $technicianUid for order $orderId. Payout: $payoutAmount")
    }

    @Transactional(readOnly = true)
    fun getDashboardSummary(technicianUid: String): WalletDashboardResponse {
        val balance = repository.getBalance(technicianUid)
        val totalEarnings = repository.getTotalLifetimeEarnings(technicianUid)
        val recentTransactions = repository.findTop20ByTechnicianIdOrderByCreatedAtDesc(technicianUid)
        
        return WalletDashboardResponse(
            balance = balance,
            totalEarnings = totalEarnings,
            recentTransactions = recentTransactions
        )
    }
}
