package com.iotabuild.zippyhand_Admin.wallet.service

import com.iotabuild.zippyhand_Admin.wallet.dto.BankAccountResponse
import com.iotabuild.zippyhand_Admin.wallet.dto.BankAccountUpdateRequest
import com.iotabuild.zippyhand_Admin.wallet.entity.BankAccount
import com.iotabuild.zippyhand_Admin.wallet.repository.BankAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BankAccountService(private val repository: BankAccountRepository) {

    fun getBankAccount(technicianUid: String): BankAccountResponse? {
        val account = repository.findByTechnicianId(technicianUid) ?: return null
        return toResponse(account)
    }

    @Transactional
    fun updateBankAccount(technicianUid: String, request: BankAccountUpdateRequest): BankAccountResponse {
        if (!request.isValid()) {
            throw Exception("Invalid account details. Account number and confirmation must match.")
        }

        val existingAccount = repository.findByTechnicianId(technicianUid)
        
        val accountToSave = if (existingAccount != null) {
            existingAccount.copy(
                accountHolderName = request.accountHolderName,
                accountNumber = request.accountNumber,
                ifscCode = request.ifscCode,
                bankName = request.bankName,
                isVerified = false,
                updatedAt = LocalDateTime.now()
            )
        } else {
            BankAccount(
                technicianId = technicianUid,
                accountHolderName = request.accountHolderName,
                accountNumber = request.accountNumber,
                ifscCode = request.ifscCode,
                bankName = request.bankName,
                isVerified = false,
                updatedAt = LocalDateTime.now()
            )
        }

        val saved = repository.save(accountToSave)
        return toResponse(saved)
    }

    private fun toResponse(account: BankAccount): BankAccountResponse {
        val raw = account.accountNumber
        val masked = if (raw.length > 4) {
            "XXXXXX" + raw.substring(raw.length - 4)
        } else {
            "XXXX"
        }

        return BankAccountResponse(
            accountHolderName = account.accountHolderName,
            maskedAccountNumber = masked,
            ifscCode = account.ifscCode,
            bankName = account.bankName,
            isVerified = account.isVerified,
            updatedAt = account.updatedAt
        )
    }
}
