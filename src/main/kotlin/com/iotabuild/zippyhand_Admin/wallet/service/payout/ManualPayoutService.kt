package com.iotabuild.zippyhand_Admin.wallet.service.payout

import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalRequest
import org.springframework.stereotype.Service

@Service("manualPayoutService")
class ManualPayoutService : PayoutService {
    override fun processPayout(request: WithdrawalRequest): PayoutResult {
        // In manual mode, we just assume the admin has handled it externally
        return PayoutResult(
            success = true,
            transferId = "MANUAL_${java.util.UUID.randomUUID()}",
            provider = "MANUAL"
        )
    }
}
