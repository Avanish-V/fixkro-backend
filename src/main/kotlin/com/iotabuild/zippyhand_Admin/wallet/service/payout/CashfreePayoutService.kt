package com.iotabuild.zippyhand_Admin.wallet.service.payout

import com.fasterxml.jackson.databind.ObjectMapper
import com.iotabuild.zippyhand_Admin.payment.service.CashfreeConfig
import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalRequest
import com.iotabuild.zippyhand_Admin.wallet.repository.BankAccountRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service("cashfreePayoutService")
class CashfreePayoutService(
    private val cashfreeConfig: CashfreeConfig,
    private val bankAccountRepository: BankAccountRepository,
    private val objectMapper: ObjectMapper
) : PayoutService {

    private val logger = LoggerFactory.getLogger(CashfreePayoutService::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun processPayout(request: WithdrawalRequest): PayoutResult {
        try {
            val bankAccount = bankAccountRepository.findById(request.bankAccountId)
                .orElseThrow { Exception("Bank account not found for payout") }

            // 1. Get Auth Token
            val token = getAuthToken()

            // 2. Add Beneficiary (optional but recommended in Cashfree, or use Direct Transfer)
            // For simplicity and speed in this demo, we'll use the Direct Transfer API if possible, 
            // but usually we add beneficiary first. Cashfree "Direct Transfer" is more common for one-offs.
            
            return executeTransfer(request, bankAccount, token)

        } catch (e: Exception) {
            logger.error("Cashfree Payout Failed for request ${request.id}: ${e.message}", e)
            return PayoutResult(
                success = false,
                errorMessage = e.message ?: "Unknown error during Cashfree payout",
                provider = "CASHFREE"
            )
        }
    }

    private fun getAuthToken(): String {
        val url = "${cashfreeConfig.getPayoutBaseUrl()}/payout/v1/authorize"
        val httpRequest = Request.Builder()
            .url(url)
            .post("{}".toRequestBody("application/json".toMediaType()))
            .addHeader("X-Client-Id", cashfreeConfig.appId)
            .addHeader("X-Client-Secret", cashfreeConfig.secretKey)
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Auth failed: Empty response")
            logger.info("Cashfree Auth Response: $body")
            
            if (!response.isSuccessful) throw Exception("Auth failed with status ${response.code}: $body")
            
            val json = objectMapper.readTree(body)
            val status = json.path("status").asText()
            
            if (status != "SUCCESS") {
                val message = json.path("message").asText("Unknown error")
                throw Exception("Cashfree Auth rejected: $message")
            }

            return json.path("data").path("token").asText().ifBlank { 
                throw Exception("Auth failed: Token not found in response") 
            }
        }
    }

    private fun executeTransfer(
        request: WithdrawalRequest, 
        bankAccount: com.iotabuild.zippyhand_Admin.wallet.entity.BankAccount, 
        token: String
    ): PayoutResult {
        val url = "${cashfreeConfig.getPayoutBaseUrl()}/payout/v1/directTransfer"
        
        val transferRequest = mapOf(
            "transferId" to "TRF_${request.id.toString().take(12)}",
            "amount" to request.amount.toString(),
            "transferMode" to "banktransfer",
            "beneficiaryDetails" to mapOf(
                "beneficiaryId" to "BEN_${request.technicianId.take(12)}",
                "bankAccount" to bankAccount.accountNumber,
                "ifsc" to bankAccount.ifscCode,
                "name" to bankAccount.accountHolderName
            )
        )

        val jsonBody = objectMapper.writeValueAsString(transferRequest)
        val httpRequest = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Transfer failed: Empty response")
            logger.info("Cashfree Transfer Response for request ${request.id}: $body")
            
            val json = objectMapper.readTree(body)
            val status = json.path("status").asText()
            
            if (response.isSuccessful && status == "SUCCESS") {
                val referenceId = json.path("data").path("referenceId").asText("SUCCESS")
                return PayoutResult(success = true, transferId = referenceId, provider = "CASHFREE")
            } else {
                val message = json.path("message").asText("Transfer rejected by Cashfree")
                val subCode = json.path("subCode").asText("")
                return PayoutResult(
                    success = false, 
                    errorMessage = if (subCode.isNotBlank()) "[$subCode] $message" else message, 
                    provider = "CASHFREE"
                )
            }
        }
    }
}
