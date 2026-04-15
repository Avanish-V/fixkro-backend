package com.iotabuild.zippyhand_Admin.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.iotabuild.zippyhand_Admin.common.exception.ApiException
import com.iotabuild.zippyhand_Admin.order.OrderRepository
import com.iotabuild.zippyhand_Admin.order.OrderService
import com.iotabuild.zippyhand_Admin.pricing.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.PricingService
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val pricingRepository: PricingService,
    private val cashfreeConfig: CashfreeConfig,
    private val objectMapper: ObjectMapper
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    companion object {
        private const val CREATE_ORDER_ENDPOINT = "/orders"
        private const val GET_ORDER_ENDPOINT = "/orders"
        private const val GET_PAYMENT_ENDPOINT = "/orders"
    }

    /**
     * Create a Cashfree payment order
     */
    @Transactional
    fun createPaymentOrder(request: CreatePaymentOrderRequest): PaymentOrderResponse {
        val totalAmount = pricingRepository.calculateFinalPrice(PriceRequest(productId = request.productId))
        val cfOrderId = "order_${generateOrderId()}_${System.currentTimeMillis()}"

        return createCashfreeSession(
            orderId = cfOrderId,
            amount = totalAmount.totalPrice.toDouble(),
            customerName = request.customerName,
            customerEmail = request.customerEmail,
            customerPhone = request.customerPhone
        )
    }

    @Transactional
    fun createCashfreeSession(
        orderId: String,
        amount: Double,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ): PaymentOrderResponse {
        // Prepare Cashfree order request
        val cashfreeRequest = CashfreeCreateOrderRequest(
            order_id = orderId,
            order_amount = amount,
            order_currency = "INR",
            customer_details = CashfreeCustomer(
                customer_id = orderId, // Or a separate customer ID if you have one
                customer_name = customerName,
                customer_email = customerEmail,
                customer_phone = customerPhone
            ),
        )

        // Call Cashfree API
        val cashfreeResponse = callCashfreeCreateOrder(cashfreeRequest)

        return PaymentOrderResponse(
            orderId = cashfreeResponse.order_id,
            cfOrderId = cashfreeResponse.cf_order_id,
            paymentSessionId = cashfreeResponse.payment_session_id,
            orderAmount = cashfreeResponse.order_amount.toInt(),
            currency = cashfreeResponse.order_currency,
        )
    }


    /**
     * Verify payment status
     */
    @Transactional
    fun verifyPayment(request: VerifyPaymentRequest): CashfreeOrderResponse {
        logger.info("Verifying payment for order ID: ${request.orderId}")

        try {
            // Fetch latest payment status from Cashfree
            val cashfreeOrder = callCashfreeGetOrder(request.orderId)

            // Update order payment status based on Cashfree response
            if (cashfreeOrder.order_status.equals("PAID", ignoreCase = true)) {
                updateOrderPaymentStatus(cashfreeOrder.order_id, com.iotabuild.zippyhand_Admin.order.PaymentStatus.PAID)
                logger.info("Payment verified and order ${cashfreeOrder.order_id} marked as PAID")
            } else {
                logger.warn("Payment verification failed for order ${cashfreeOrder.order_id}: Status is ${cashfreeOrder.order_status}")
                throw PaymentVerificationException("Payment is not successful. Current status: ${cashfreeOrder.order_status}")
            }

            return cashfreeOrder

        } catch (e: CashfreeApiException) {
            logger.error("Cashfree API error during verification: ${e.message}", e)
            throw PaymentVerificationException("Unable to verify payment: ${e.message}")
        } catch (e: Exception) {
            logger.error("Payment verification failed", e)
            throw PaymentVerificationException(e.message ?: "Unknown error")
        }
    }


    /**
     * Handle Cashfree webhook
     */
    @Transactional
    fun handleWebhook(webhookPayload: String, signature: String?): Boolean {
        logger.info("Processing Cashfree webhook")

        try {
            // Verify webhook signature
            if (!verifyWebhookSignature(webhookPayload, signature)) {
                logger.warn("Invalid webhook signature")
                throw InvalidWebhookSignatureException()
            }

            // Parse webhook data
            val webhookData: CashfreeWebhookData = objectMapper.readValue(webhookPayload)

            webhookData.order?.let { orderData ->
                val payment = paymentRepository.findByCfOrderId(orderData.order_id)
                    .orElseThrow {
                        logger.warn("Payment not found for webhook order: ${orderData.order_id}")
                        PaymentNotFoundException("0")
                    }

                // Update payment status from order data
                payment.paymentStatus = mapCashfreeStatus(orderData.order_status)
                payment.updatedAt = LocalDateTime.now()
                payment.webhookData = webhookPayload

                // Update payment details if payment data exists
                webhookData.payment?.let { paymentData ->
                    payment.cfPaymentId = paymentData.cf_payment_id
                    payment.paymentMode = paymentData.payment_method?.get("payment_method")?.toString()
                    payment.paymentMessage = paymentData.payment_message
                    payment.bankReference = paymentData.bank_reference

                    paymentData.payment_time?.let {
                        payment.paymentTime = parsePaymentTime(it)
                    }
                }

                paymentRepository.save(payment)

                // Update order status
                when (payment.paymentStatus) {
                    PaymentStatus.SUCCESS -> {
                        updateOrderPaymentStatus(payment.orderId, com.iotabuild.zippyhand_Admin.order.PaymentStatus.PAID)
                        logger.info("Webhook processed: Payment SUCCESS for order ${payment.orderId}")
                    }
                    PaymentStatus.FAILED -> {
                        updateOrderPaymentStatus(payment.orderId, com.iotabuild.zippyhand_Admin.order.PaymentStatus.FAILED)
                        logger.info("Webhook processed: Payment FAILED for order ${payment.orderId}")
                    }
                    else -> {
                        logger.info("Webhook processed: Payment status ${payment.paymentStatus} for order ${payment.orderId}")
                    }
                }
            }

            return true

        } catch (e: Exception) {
            logger.error("Webhook processing failed", e)
            throw e
        }
    }

    /**
     * Get payment status
     */


    /* ---------- PRIVATE HELPER METHODS ---------- */

    private fun callCashfreeCreateOrder(request: CashfreeCreateOrderRequest): CashfreeOrderResponse {
        val url = "${cashfreeConfig.getBaseUrl()}$CREATE_ORDER_ENDPOINT"
        val jsonBody = objectMapper.writeValueAsString(request)

        logger.info("Creating Cashfree order: URL=$url")
        logger.debug("Request body: $jsonBody")

        val httpRequest = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .addHeader("x-client-id", cashfreeConfig.getAppId())
            .addHeader("x-client-secret", cashfreeConfig.getSecretKey())
            .addHeader("x-api-version", cashfreeConfig.getApiVersion())
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: throw CashfreeApiException("Empty response from Cashfree")

                logger.info("Cashfree API response: code=${response.code}, body=$responseBody")

                if (!response.isSuccessful) {
                    logger.error("Cashfree API error: ${response.code} - $responseBody")
                    throw CashfreeApiException("Failed to create order: ${response.code} - $responseBody")
                }

                return objectMapper.readValue(responseBody)
            }
        } catch (e: java.net.SocketTimeoutException) {
            logger.error("Cashfree API timeout: Unable to connect to $url", e)
            throw CashfreeApiException("Cashfree API timeout. Please check your network connection or try again later.", e)
        } catch (e: java.io.IOException) {
            logger.error("Cashfree API network error: ${e.message}", e)
            throw CashfreeApiException("Network error while connecting to Cashfree: ${e.message}", e)
        }
    }

    private fun callCashfreeGetOrder(cfOrderId: String): CashfreeOrderResponse {
        val url = "${cashfreeConfig.getBaseUrl()}$GET_ORDER_ENDPOINT/$cfOrderId"

        logger.info("Fetching Cashfree order: URL=$url, cfOrderId=$cfOrderId")

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-client-id", cashfreeConfig.getAppId())
            .addHeader("x-client-secret", cashfreeConfig.getSecretKey())
            .addHeader("x-api-version", cashfreeConfig.getApiVersion())
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: throw CashfreeApiException("Empty response from Cashfree")

            logger.info("Cashfree get order response: code=${response.code}, body=$responseBody")

            if (!response.isSuccessful) {
                logger.error("Cashfree API error: ${response.code} - $responseBody")
                throw CashfreeApiException("Failed to get order: ${response.code} - $responseBody")
            }

            return objectMapper.readValue(responseBody)
        }
    }

    private fun callCashfreeGetPayments(cfOrderId: String): List<CashfreePaymentResponse> {
        val url = "${cashfreeConfig.getBaseUrl()}$GET_PAYMENT_ENDPOINT/$cfOrderId/payments"

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-client-id", cashfreeConfig.getAppId())
            .addHeader("x-client-secret", cashfreeConfig.getSecretKey())
            .addHeader("x-api-version", cashfreeConfig.getApiVersion())
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: return emptyList()

            if (!response.isSuccessful) {
                logger.warn("Failed to get payments: ${response.code}")
                return emptyList()
            }

            return try {
                objectMapper.readValue(responseBody)
            } catch (e: Exception) {
                logger.warn("Failed to parse payments response", e)
                emptyList()
            }
        }
    }

    private fun mapCashfreeStatus(cashfreeStatus: String): PaymentStatus {
        return when (cashfreeStatus.uppercase()) {
            "ACTIVE" -> PaymentStatus.ACTIVE
            "PAID", "SUCCESS" -> PaymentStatus.SUCCESS
            "EXPIRED" -> PaymentStatus.EXPIRED
            "CANCELLED" -> PaymentStatus.CANCELLED
            "FAILED", "USER_DROPPED" -> PaymentStatus.FAILED
            "PARTIAL" -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }
    }

    private fun generatePaymentLink(sessionId: String): String {
        // Cashfree checkout page URL
        return if (cashfreeConfig.isProduction()) {
            "https://payments.cashfree.com/order/$sessionId"
        } else {
            "https://sandbox.cashfree.com/pg/view/order/$sessionId"
        }
    }

    private fun verifyWebhookSignature(payload: String, signature: String?): Boolean {
        if (signature == null) {
            logger.warn("No signature provided in webhook")
            return false
        }

        try {
            val secretKey = cashfreeConfig.getSecretKey()
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
            hmac.init(secretKeySpec)

            val computedSignature = Base64.getEncoder().encodeToString(hmac.doFinal(payload.toByteArray()))

            return computedSignature == signature
        } catch (e: Exception) {
            logger.error("Failed to verify webhook signature", e)
            return false
        }
    }

    private fun parsePaymentTime(timeString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            logger.warn("Failed to parse payment time: $timeString", e)
            LocalDateTime.now()
        }
    }

    private fun updateOrderPaymentStatus(orderId: String, status: com.iotabuild.zippyhand_Admin.order.PaymentStatus) {
        try {
            val order = orderRepository.findByOrderId(orderId)
                .orElseThrow { RuntimeException("Order not found with orderId: $orderId") }
            order.paymentStatus = status
            orderRepository.save(order)
            logger.info("Updated order $orderId payment status to $status")
        } catch (e: Exception) {
            logger.error("Failed to update order payment status", e)
        }
    }

    private fun mapToStatusResponse(payment: Payment): PaymentStatusResponse {
        return PaymentStatusResponse(
            orderId = payment.orderId.toString(),
            cfOrderId = payment.cfOrderId,
            cfPaymentId = payment.cfPaymentId,
            paymentStatus = payment.paymentStatus,
            amount = payment.amount,
            currency = payment.currency,
            paymentMode = payment.paymentMode,
            paymentMessage = payment.paymentMessage,
            bankReference = payment.bankReference,
            paymentTime = payment.paymentTime,
            createdAt = payment.createdAt,
            updatedAt = payment.updatedAt
        )
    }
}
fun generateOrderId(): Int {
    return Random.nextInt(10000, 100000)
}
