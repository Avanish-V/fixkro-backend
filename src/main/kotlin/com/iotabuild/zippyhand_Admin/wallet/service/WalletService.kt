package com.iotabuild.zippyhand_Admin.wallet.service

import com.iotabuild.zippyhand_Admin.wallet.dto.WalletDashboardResponse
import com.iotabuild.zippyhand_Admin.wallet.entity.*
import com.iotabuild.zippyhand_Admin.wallet.repository.BankAccountRepository
import com.iotabuild.zippyhand_Admin.wallet.repository.WalletLedgerRepository
import com.iotabuild.zippyhand_Admin.wallet.repository.WithdrawalRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import org.slf4j.LoggerFactory

@Service
class WalletService(
    private val repository: WalletLedgerRepository,
    private val withdrawalRequestRepository: WithdrawalRequestRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val payoutServices: Map<String, com.iotabuild.zippyhand_Admin.wallet.service.payout.PayoutService>
) {
    private val logger = LoggerFactory.getLogger(WalletService::class.java)

    @Transactional
    fun recordJobEarning(
        technicianUid: String,
        orderId: String,
        jobTitle: String,
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
            title = jobTitle,
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

    @Transactional
    fun requestWithdrawal(technicianUid: String, amount: BigDecimal, idempotencyKey: String? = null): WithdrawalRequest {
        if (amount <= BigDecimal.ZERO) throw IllegalArgumentException("Amount must be greater than zero")

        // Check for existing request with the same idempotency key
        if (idempotencyKey != null) {
            val existing = withdrawalRequestRepository.findByIdempotencyKey(idempotencyKey)
            if (existing != null) {
                if (existing.technicianId != technicianUid) {
                    throw IllegalStateException("Idempotency key collision")
                }
                return existing
            }
        }

        val currentBalance = repository.getBalance(technicianUid)
        if (currentBalance < amount) {
            throw IllegalStateException("Insufficient balance. Current balance: $currentBalance")
        }

        val bankAccount = bankAccountRepository.findByTechnicianId(technicianUid)
            ?: throw IllegalStateException("No bank account found. Please add bank details first.")

        val withdrawalRequest = WithdrawalRequest(
            technicianId = technicianUid,
            amount = amount,
            bankAccountId = bankAccount.id,
            status = WithdrawalStatus.PENDING,
            idempotencyKey = idempotencyKey
        )
        val savedRequest = withdrawalRequestRepository.save(withdrawalRequest)

        val ledgerEntry = WalletLedger(
            technicianId = technicianUid,
            type = TransactionType.DEBIT,
            entryType = EntryType.WITHDRAWAL,
            amount = amount,
            referenceId = savedRequest.id.toString(),
            referenceType = "WITHDRAWAL_REQUEST",
            title = "Wallet Withdrawal",
            description = "Withdrawal request for amount $amount",
            idempotencyKey = "withdraw_${savedRequest.id}",
            status = "PENDING"
        )

        repository.save(ledgerEntry)
        logger.info("Withdrawal request of $amount initiated for technician $technicianUid")
        
        return savedRequest
    }

    @Transactional(readOnly = true)
    fun getPartnerWithdrawals(technicianUid: String): List<WithdrawalRequest> {
        return withdrawalRequestRepository.findByTechnicianIdOrderByCreatedAtDesc(technicianUid)
    }

    @Transactional(readOnly = true)
    fun getAllWithdrawals(): List<WithdrawalRequest> {
        return withdrawalRequestRepository.findAll().sortedByDescending { it.createdAt }
    }

    @Transactional
    fun updateWithdrawalStatus(
        requestId: java.util.UUID, 
        newStatus: WithdrawalStatus, 
        remarks: String? = null,
        provider: String = "manualPayoutService"
    ): WithdrawalRequest {
        val request = withdrawalRequestRepository.findById(requestId)
            .orElseThrow { Exception("Withdrawal request not found") }
        
        if (request.status != WithdrawalStatus.PENDING) {
            throw IllegalStateException("Only pending requests can be updated (Current status: ${request.status})")
        }

        // If approving, trigger the actual payout
        if (newStatus == WithdrawalStatus.COMPLETED || newStatus == WithdrawalStatus.APPROVED) {
            val payoutService = payoutServices[provider] ?: throw Exception("Payout provider $provider not found")
            
            val result = payoutService.processPayout(request)
            if (!result.success) {
                throw Exception("Payout failed via ${result.provider}: ${result.errorMessage}")
            }
            
            request.status = WithdrawalStatus.COMPLETED
            request.remarks = (remarks ?: "") + " | Payout ID: ${result.transferId} via ${result.provider}"
        } else {
            request.status = newStatus
            request.remarks = remarks
        }

        request.processedAt = java.time.OffsetDateTime.now()

        // Update the corresponding ledger entry
        val ledgerEntry = repository.findByReferenceIdAndReferenceType(request.id.toString(), "WITHDRAWAL_REQUEST")
            ?: throw Exception("Ledger entry not found for withdrawal request")
        
        if (request.status == WithdrawalStatus.COMPLETED) {
            ledgerEntry.status = "SETTLED"
        } else if (newStatus == WithdrawalStatus.REJECTED) {
            ledgerEntry.status = "REJECTED"
        }

        repository.save(ledgerEntry)
        return withdrawalRequestRepository.save(request)
    }
}
