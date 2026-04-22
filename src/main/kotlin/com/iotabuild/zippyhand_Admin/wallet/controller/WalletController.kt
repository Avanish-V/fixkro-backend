package com.iotabuild.zippyhand_Admin.wallet.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.wallet.dto.BankAccountResponse
import com.iotabuild.zippyhand_Admin.wallet.dto.BankAccountUpdateRequest
import com.iotabuild.zippyhand_Admin.wallet.dto.WalletDashboardResponse
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
}
