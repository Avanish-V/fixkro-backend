package com.iotabuild.zippyhand_Admin.wallet.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalRequest
import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalStatus
import com.iotabuild.zippyhand_Admin.wallet.service.WalletService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/wallet")
class AdminWalletController(private val walletService: WalletService) {

    @GetMapping("/withdrawals")
    fun getAllWithdrawals(): ApiResponse<List<WithdrawalRequest>> {
        return ApiResponse.success(walletService.getAllWithdrawals())
    }

    @PostMapping("/withdrawals/{id}/approve")
    fun approveWithdrawal(
        @PathVariable id: UUID,
        @RequestParam(required = false) remarks: String?,
        @RequestParam(defaultValue = "cashfreePayoutService") provider: String
    ): ApiResponse<WithdrawalRequest> {
        val updated = walletService.updateWithdrawalStatus(id, WithdrawalStatus.COMPLETED, remarks, provider)
        return ApiResponse.success(updated)
    }

    @PostMapping("/withdrawals/{id}/reject")
    fun rejectWithdrawal(
        @PathVariable id: UUID,
        @RequestParam(required = false) remarks: String?
    ): ApiResponse<WithdrawalRequest> {
        val updated = walletService.updateWithdrawalStatus(id, WithdrawalStatus.REJECTED, remarks ?: "Rejected by Admin")
        return ApiResponse.success(updated)
    }
}
