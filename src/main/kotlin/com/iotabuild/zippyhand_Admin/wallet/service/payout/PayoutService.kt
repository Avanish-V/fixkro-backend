package com.iotabuild.zippyhand_Admin.wallet.service.payout

import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalRequest
import java.math.BigDecimal

interface PayoutService {
    /**
     * Executes the actual payout to the partner's bank account.
     * returns a unique transfer ID from the provider if successful.
     */
    fun processPayout(request: WithdrawalRequest): PayoutResult
}

data class PayoutResult(
    val success: Boolean,
    val transferId: String? = null,
    val errorMessage: String? = null,
    val provider: String
)
