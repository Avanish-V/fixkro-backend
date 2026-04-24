package com.iotabuild.zippyhand_Admin.wallet.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.wallet.dto.*
import com.iotabuild.zippyhand_Admin.wallet.service.BankAccountService
import com.iotabuild.zippyhand_Admin.wallet.service.WalletService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/partners/wallet")
class WalletController(
    private val walletService: WalletService,
    private val bankAccountService: BankAccountService
) {

    @GetMapping("/dashboard")
    fun getDashboard(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<WalletDashboardResponse> {
        val summary = walletService.getDashboardSummary(token.uid)
        return ApiResponse.success(summary)
    }

    @GetMapping("/bank-account")
    fun getBankAccount(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<BankAccountResponse?> {
        val account = bankAccountService.getBankAccount(token.uid)
        return ApiResponse.success(account)
    }

    @PostMapping("/bank-account")
    fun updateBankAccount(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: BankAccountUpdateRequest
    ): ApiResponse<BankAccountResponse> {
        val updated = bankAccountService.updateBankAccount(token.uid, request)
        return ApiResponse.success(updated)
    }

    @PostMapping("/withdraw")
    fun requestWithdrawal(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: WithdrawalRequestDto
    ): ApiResponse<WithdrawalResponse> {
        val withdrawal = walletService.requestWithdrawal(token.uid, request.amount, request.idempotencyKey)
        val response = WithdrawalResponse(
            id = withdrawal.id.toString(),
            amount = withdrawal.amount,
            status = withdrawal.status.name,
            createdAt = withdrawal.createdAt
        )
        return ApiResponse.success(response)
    }

    @GetMapping("/withdrawals")
    fun getWithdrawals(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<List<WithdrawalResponse>> {
        val withdrawals = walletService.getPartnerWithdrawals(token.uid)
        val response = withdrawals.map {
            WithdrawalResponse(
                id = it.id.toString(),
                amount = it.amount,
                status = it.status.name,
                createdAt = it.createdAt
            )
        }
        return ApiResponse.success(response)
    }
}
