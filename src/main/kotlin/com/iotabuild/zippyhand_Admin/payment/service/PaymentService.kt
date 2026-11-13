package com.iotabuild.zippyhand_Admin.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.iotabuild.zippyhand_Admin.order.service.OrderLifecycleService
import com.iotabuild.zippyhand_Admin.payment.dto.*
import com.iotabuild.zippyhand_Admin.payment.entity.Payment
import com.iotabuild.zippyhand_Admin.payment.entity.PaymentStatus
import com.iotabuild.zippyhand_Admin.payment.exception.*
import com.iotabuild.zippyhand_Admin.payment.repository.PaymentRepository
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.service.PricingService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderLifecycleService: OrderLifecycleService,
    private val pricingService: PricingService,
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

    @Transactional
    fun createPaymentOrder(request: CreatePaymentOrderRequest): PaymentOrderResponse {
        val totalAmount = pricingService.calculateFinalPrice(PriceRequest(productId = request.productId))
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
        customerPhone: String,
        paymentMode: String = "UPI"
    ): PaymentOrderResponse {
        val cashfreeRequest = CashfreeCreateOrderRequest(
            order_id = orderId,
            order_amount = amount,
            order_currency = "INR",
            customer_details = CashfreeCustomer(
                customer_id = orderId,
                customer_name = customerName,
                customer_email = customerEmail,
                customer_phone = customerPhone
            ),
            order_meta = CashfreeOrderMeta(
                // return_url: Cashfree redirects user here after payment (only {order_id} supported in API v2023-08-01+)
                return_url = "${cashfreeConfig.returnUrl}?order_id={order_id}",
                // notify_url: Cashfree POSTs payment events here — must be publicly reachable
                notify_url = cashfreeConfig.notifyUrl.ifBlank { null }
            )
        )

        logger.info("CREATING CASHFREE SESSION: orderId={}, amount={}, customer={}", orderId, amount, customerName)
        if (amount <= 1.0) {
            logger.warn("WARNING: Amount is very low ({}). Payment gateway might fail or show 1 rupee.", amount)
        }
        val cashfreeResponse = callCashfreeCreateOrder(cashfreeRequest)

        // Maintain the payment table record along with the order
        val payment = paymentRepository.findByOrderId(orderId).orElseGet {
            Payment(
                orderId = orderId,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone,
                amount = cashfreeResponse.order_amount.toInt(),
                currency = cashfreeResponse.order_currency
            )
        }

        payment.cfOrderId = cashfreeResponse.getCfOrderIdStr()
        payment.paymentSessionId = cashfreeResponse.payment_session_id
        payment.customerName = customerName
        payment.customerEmail = customerEmail
        payment.customerPhone = customerPhone
        payment.amount = cashfreeResponse.order_amount.toInt()
        payment.currency = cashfreeResponse.order_currency
        payment.paymentStatus = PaymentStatus.PENDING
        payment.paymentMode = paymentMode
        payment.updatedAt = LocalDateTime.now()

        val savedPayment = paymentRepository.save(payment)
        logger.info("Maintained payment table record: paymentId={}, orderId={}, cfOrderId={}", savedPayment.id, orderId, savedPayment.cfOrderId)

        return PaymentOrderResponse(
            orderId = cashfreeResponse.order_id,
            cfOrderId = cashfreeResponse.getCfOrderIdStr(),
            paymentSessionId = cashfreeResponse.payment_session_id,
            orderAmount = cashfreeResponse.order_amount.toInt(),
            currency = cashfreeResponse.order_currency,
            paymentId = savedPayment.id
        )
    }

    /**
     * Used for CHECKING order payment status without manual verification requirement.
     * Webhook is the primary automated updater in production.
     * If the order is already verified via webhook, returns status immediately.
     * If webhook is delayed, queries Cashfree order and payment attempts, synchronizing database in a single transaction.
     */
    @Transactional
    fun verifyPayment(request: VerifyPaymentRequest): CashfreeOrderResponse {
        val orderId = request.orderId.trim()
        logger.info("Checking payment status for order ID: {}", orderId)

        // 1. Check local database first: if already marked as PAID by webhook, return immediately
        val localOrder = orderLifecycleService.findByOrderId(orderId)
        if (localOrder != null && localOrder.paymentStatus == com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID) {
            logger.info("Order {} is already confirmed PAID in database via webhook", orderId)
            return CashfreeOrderResponse(
                cf_order_id = localOrder.payment?.cfOrderId ?: orderId,
                order_id = localOrder.orderId,
                entity = "order",
                order_currency = "INR",
                order_amount = localOrder.totalAmount.toDouble(),
                order_status = "PAID",
                payment_session_id = localOrder.payment?.paymentSessionId ?: ""
            )
        }

        // 2. Query Cashfree to check gateway order status and individual payment attempts
        try {
            // Cashfree PG GET /orders/{order_id} expects the merchant order_id (e.g. ORD261108W5Y9)
            // Never pass Cashfree's internal numeric cf_order_id (e.g. 1473010435090561024) which causes 404
            val queryOrderId = localOrder?.orderId?.takeIf { it.isNotBlank() } ?: orderId
            val cashfreeOrder = try {
                callCashfreeGetOrder(queryOrderId)
            } catch (e: Exception) {
                // Fallback in case cfOrderId was used in mocks or legacy records
                val fallbackId = localOrder?.payment?.cfOrderId
                if (!fallbackId.isNullOrBlank() && fallbackId != queryOrderId) {
                    callCashfreeGetOrder(fallbackId)
                } else throw e
            }
            val orderStatusUpper = cashfreeOrder.order_status.uppercase()

            if (orderStatusUpper in listOf("PAID", "SUCCESS")) {
                orderLifecycleService.markPaymentFromWebhook(
                    orderId = localOrder?.orderId ?: cashfreeOrder.order_id,
                    targetPaymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID,
                    cfOrderId = cashfreeOrder.getCfOrderIdStr(),
                    paymentMessage = "Verified via Cashfree GetOrder check"
                )
                logger.info("Order {} marked as PAID from Cashfree status check", cashfreeOrder.order_id)
                return cashfreeOrder.copy(order_status = "PAID")
            }

            if (orderStatusUpper in listOf("EXPIRED", "TERMINATED")) {
                orderLifecycleService.markPaymentFromWebhook(
                    orderId = localOrder?.orderId ?: cashfreeOrder.order_id,
                    targetPaymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.FAILED,
                    cfOrderId = cashfreeOrder.getCfOrderIdStr(),
                    paymentMessage = "Payment status from Cashfree: $orderStatusUpper"
                )
                return cashfreeOrder.copy(order_status = "FAILED")
            }

            // If Cashfree order status is ACTIVE, inspect individual payment attempts
            val payments = try {
                val list = callCashfreeGetPayments(queryOrderId)
                if (list.isNotEmpty()) list
                else {
                    val fallbackId = localOrder?.payment?.cfOrderId
                    if (!fallbackId.isNullOrBlank() && fallbackId != queryOrderId) {
                        callCashfreeGetPayments(fallbackId)
                    } else list
                }
            } catch (e: Exception) {
                emptyList()
            }
            logger.info("Cashfree returned {} payment attempt(s) for order {}", payments.size, queryOrderId)

            val successAttempt = payments.find { it.payment_status.equals("SUCCESS", ignoreCase = true) }
            if (successAttempt != null) {
                val mappedMode = successAttempt.payment_group?.let { parsePaymentMode(it) }
                val paymentTime = successAttempt.payment_time?.let { parsePaymentTime(it) }

                orderLifecycleService.markPaymentFromWebhook(
                    orderId = localOrder?.orderId ?: cashfreeOrder.order_id,
                    targetPaymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID,
                    cfOrderId = cashfreeOrder.getCfOrderIdStr(),
                    cfPaymentId = successAttempt.getCfPaymentIdStr(),
                    bankReference = successAttempt.bank_reference,
                    paymentMessage = successAttempt.payment_message ?: "Verified payment attempt SUCCESS",
                    paymentTime = paymentTime,
                    paymentMode = mappedMode
                )
                logger.info("Order {} marked as PAID from payment attempt {}", cashfreeOrder.order_id, successAttempt.getCfPaymentIdStr())
                return cashfreeOrder.copy(order_status = "PAID")
            }

            val pendingAttempt = payments.find { it.payment_status.equals("PENDING", ignoreCase = true) }
            if (pendingAttempt != null) {
                logger.info("Order {} has pending payment attempt: {}", cashfreeOrder.order_id, pendingAttempt.getCfPaymentIdStr())
                return cashfreeOrder.copy(order_status = "PENDING")
            }

            val hasFailedAttempts = payments.isNotEmpty() && payments.all {
                it.payment_status.uppercase() in listOf("FAILED", "USER_DROPPED", "CANCELLED", "VOID")
            }
            if (hasFailedAttempts) {
                val latestAttempt = payments.lastOrNull()
                logger.info("All payment attempt(s) failed for order {}", cashfreeOrder.order_id)
                orderLifecycleService.markPaymentFromWebhook(
                    orderId = localOrder?.orderId ?: cashfreeOrder.order_id,
                    targetPaymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.FAILED,
                    cfOrderId = cashfreeOrder.getCfOrderIdStr(),
                    cfPaymentId = latestAttempt?.getCfPaymentIdStr(),
                    bankReference = latestAttempt?.bank_reference,
                    paymentMessage = latestAttempt?.payment_message ?: "Payment attempt failed"
                )
                return cashfreeOrder.copy(order_status = "FAILED")
            }

            // No payment attempts completed yet, but order is ACTIVE
            logger.info("Order {} is ACTIVE with no completed attempts yet", cashfreeOrder.order_id)
            return cashfreeOrder.copy(order_status = "PENDING")

        } catch (e: Exception) {
            logger.error("Error while checking payment status with Cashfree: {}", e.message)
            if (localOrder != null) {
                return CashfreeOrderResponse(
                    cf_order_id = localOrder.payment?.cfOrderId ?: orderId,
                    order_id = localOrder.orderId,
                    entity = "order",
                    order_currency = "INR",
                    order_amount = localOrder.totalAmount.toDouble(),
                    order_status = if (localOrder.paymentStatus == com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID) "PAID" else "PENDING",
                    payment_session_id = localOrder.payment?.paymentSessionId ?: ""
                )
            }
            throw PaymentVerificationException("Unable to check payment status: ${e.message}")
        }
    }

    /**
     * Retries payment for an existing internal order.
     * 1. Reuses existing Cashfree order and paymentSessionId if still ACTIVE.
     * 2. Prevents retry if order is already PAID or if a payment attempt is PENDING.
     * 3. Creates a new Cashfree session for the SAME order only if expired/unusable.
     * 4. NEVER creates a new internal booking order.
     */
    @Transactional
    fun retryPayment(orderId: String): PaymentOrderResponse {
        val trimmedOrderId = orderId.trim()
        val localOrder = orderLifecycleService.findByOrderId(trimmedOrderId)
            ?: throw NoSuchElementException("Order not found with orderId: $trimmedOrderId")

        // 1. If already PAID, never allow retry
        if (localOrder.paymentStatus == com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID) {
            throw IllegalStateException("Payment is already completed for order: $trimmedOrderId")
        }

        val queryOrderId = localOrder.orderId
        val fallbackId = localOrder.payment?.cfOrderId

        // 2. Inspect individual payment attempts to detect any ongoing pending attempt or recent success
        try {
            val payments = try {
                val list = callCashfreeGetPayments(queryOrderId)
                if (list.isNotEmpty()) list
                else {
                    if (!fallbackId.isNullOrBlank() && fallbackId != queryOrderId) {
                        callCashfreeGetPayments(fallbackId)
                    } else list
                }
            } catch (e: Exception) {
                emptyList()
            }

            val successAttempt = payments.find { it.payment_status.equals("SUCCESS", ignoreCase = true) }
            if (successAttempt != null) {
                orderLifecycleService.markPaymentFromWebhook(
                    orderId = localOrder.orderId,
                    targetPaymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID,
                    cfOrderId = fallbackId,
                    cfPaymentId = successAttempt.getCfPaymentIdStr(),
                    bankReference = successAttempt.bank_reference,
                    paymentMessage = successAttempt.payment_message ?: "Verified payment attempt SUCCESS"
                )
                throw IllegalStateException("Payment has already succeeded for order: $trimmedOrderId")
            }

            val hasPending = payments.any { it.payment_status.equals("PENDING", ignoreCase = true) }
            if (hasPending) {
                throw IllegalStateException("A payment attempt is currently pending. Please check status.")
            }
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Could not check payment attempts before retry for {}: {}", queryOrderId, e.message)
        }

        // 3. Prefer reusing existing valid Cashfree order if it is still ACTIVE
        try {
            val existingCfOrder = try {
                callCashfreeGetOrder(queryOrderId)
            } catch (e: Exception) {
                if (!fallbackId.isNullOrBlank() && fallbackId != queryOrderId) {
                    callCashfreeGetOrder(fallbackId)
                } else throw e
            }
            if (existingCfOrder.order_status.equals("ACTIVE", ignoreCase = true) &&
                existingCfOrder.payment_session_id.isNotBlank()
            ) {
                logger.info("Reusing existing Cashfree order session for retry: orderId={}, cfOrderId={}", trimmedOrderId, existingCfOrder.getCfOrderIdStr())
                return PaymentOrderResponse(
                    orderId = localOrder.orderId,
                    cfOrderId = existingCfOrder.getCfOrderIdStr(),
                    paymentSessionId = existingCfOrder.payment_session_id,
                    orderAmount = existingCfOrder.order_amount.toInt(),
                    currency = existingCfOrder.order_currency,
                    paymentId = localOrder.payment?.id
                )
            }
        } catch (e: Exception) {
            logger.warn("Existing Cashfree order {} cannot be reused: {}", queryOrderId, e.message)
        }

        // 4. If existing order is expired or cannot be reused, create a new Cashfree session for this order
        val newCfOrderId = "${trimmedOrderId}_R${(System.currentTimeMillis() % 100000)}"
        logger.info("Creating new Cashfree order session for retry: internalOrderId={}, newCfOrderId={}", trimmedOrderId, newCfOrderId)

        val session = createCashfreeSession(
            orderId = newCfOrderId,
            amount = localOrder.totalAmount.toDouble(),
            customerName = localOrder.fullName,
            customerEmail = localOrder.payment?.customerEmail ?: "customer@example.com",
            customerPhone = localOrder.phone,
            paymentMode = localOrder.paymentMode.name
        )

        // Update local OrderPayment to reference new cfOrderId and paymentSessionId
        localOrder.payment?.let { op ->
            op.cfOrderId = session.cfOrderId
            op.paymentSessionId = session.paymentSessionId
            op.paymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PENDING
            op.updatedAt = LocalDateTime.now()
        }
        localOrder.paymentStatus = com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PENDING
        orderLifecycleService.saveOrder(localOrder)

        return session.copy(orderId = localOrder.orderId)
    }

    /**
     * Dedicated read-only status check endpoint for order payment status.
     */
    @Transactional(readOnly = true)
    fun checkPaymentStatus(orderId: String): PaymentCheckStatusResponse {
        val trimmedOrderId = orderId.trim()
        val order = orderLifecycleService.findByOrderId(trimmedOrderId)
            ?: throw NoSuchElementException("Order not found with orderId: $trimmedOrderId")

        val payment = order.payment
        val isPaid = (order.paymentStatus == com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID)

        return PaymentCheckStatusResponse(
            orderId = order.orderId,
            cfOrderId = payment?.cfOrderId,
            isPaid = isPaid,
            paymentStatus = order.paymentStatus,
            orderStatus = order.status,
            amount = order.totalAmount,
            paymentMode = order.paymentMode,
            cfPaymentId = payment?.cfPaymentId,
            bankReference = payment?.bankReference,
            paymentTime = payment?.paymentTime,
            message = if (isPaid) "Payment successfully received and confirmed" else "Payment is currently ${order.paymentStatus}"
        )
    }

    /**
     * Webhook entry point:
     * 1. Verifies Cashfree signature.
     * 2. Resiliently parses payload (supporting both root and data-nested structures).
     * 3. Atomically updates BOTH orders and orders_payment tables in ONE transaction.
     */
    @Transactional
    fun handleWebhook(webhookPayload: String, signature: String?, timestamp: String? = null): Boolean {
        logger.info("Processing Cashfree webhook. Signature present: {}, Timestamp: {}", !signature.isNullOrBlank(), timestamp)

        // 1. Signature Verification
        val isValid = verifyWebhookSignature(webhookPayload, signature, timestamp)
        if (!isValid) {
            if (cashfreeConfig.isProduction()) {
                logger.error("CRITICAL: Invalid Cashfree webhook signature in PRODUCTION!")
                throw InvalidWebhookSignatureException()
            } else {
                logger.warn("WARNING: Cashfree webhook signature verification failed in Sandbox/Dev mode - processing anyway for testing.")
            }
        }

        // 2. Resilient JSON Parsing (supports both modern data-wrapped and flat Cashfree webhook formats)
        val webhookRoot = try {
            objectMapper.readValue<CashfreeWebhookRoot>(webhookPayload)
        } catch (e: Exception) {
            logger.error("Failed to parse webhook JSON payload: {}", e.message)
            throw IllegalArgumentException("Malformed webhook payload")
        }

        val orderData = webhookRoot.resolveOrder()
        val paymentData = webhookRoot.resolvePayment()
        val eventType = webhookRoot.type

        val orderId = orderData?.order_id?.takeIf { it.isNotBlank() }
            ?: extractOrderIdFromJson(webhookPayload)

        if (orderId.isNullOrBlank()) {
            logger.warn("No order_id identified in webhook payload. Payload: {}", webhookPayload)
            return false
        }

        // 3. Status Determination
        val isSuccess = (eventType == "PAYMENT_SUCCESS_WEBHOOK") ||
                paymentData?.payment_status?.equals("SUCCESS", ignoreCase = true) == true ||
                orderData?.order_status?.equals("PAID", ignoreCase = true) == true

        val isFailed = (eventType in listOf("PAYMENT_FAILED_WEBHOOK", "PAYMENT_USER_DROPPED_WEBHOOK")) ||
                paymentData?.payment_status?.uppercase() in listOf("FAILED", "USER_DROPPED", "CANCELLED") ||
                orderData?.order_status?.uppercase() in listOf("FAILED", "EXPIRED", "CANCELLED")

        val targetStatus = when {
            isSuccess -> com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PAID
            isFailed -> com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.FAILED
            else -> com.iotabuild.zippyhand_Admin.order.entity.PaymentStatus.PENDING
        }

        val mappedMode = paymentData?.payment_group?.let { parsePaymentMode(it) }
            ?: paymentData?.payment_method?.let { parsePaymentMode(it.toString()) }

        val paymentTime = paymentData?.payment_time?.let { parsePaymentTime(it) }

        // 4. Update BOTH orders and orders_payment in ONE atomic transaction
        try {
            orderLifecycleService.markPaymentFromWebhook(
                orderId = orderId,
                targetPaymentStatus = targetStatus,
                cfOrderId = null,
                cfPaymentId = paymentData?.cf_payment_id,
                bankReference = paymentData?.bank_reference,
                paymentMessage = paymentData?.payment_message,
                paymentTime = paymentTime,
                paymentMode = mappedMode,
                webhookPayload = webhookPayload
            )
        } catch (e: NoSuchElementException) {
            // Handle concurrency race: if webhook arrived before createOrder transaction finished committing
            logger.warn("Order {} not found immediately. Retrying in 300ms...", orderId)
            Thread.sleep(300)
            orderLifecycleService.markPaymentFromWebhook(
                orderId = orderId,
                targetPaymentStatus = targetStatus,
                cfOrderId = null,
                cfPaymentId = paymentData?.cf_payment_id,
                bankReference = paymentData?.bank_reference,
                paymentMessage = paymentData?.payment_message,
                paymentTime = paymentTime,
                paymentMode = mappedMode,
                webhookPayload = webhookPayload
            )
        }

        logger.info("Webhook handled successfully for order {} with targetStatus={}", orderId, targetStatus)
        return true
    }

    @Transactional
    fun savePayment(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }

    @Transactional(readOnly = true)
    fun getPaymentByOrderId(orderId: String): Optional<Payment> {
        return paymentRepository.findByOrderId(orderId)
    }

    @Transactional(readOnly = true)
    fun getPaymentById(id: Long): Optional<Payment> {
        return paymentRepository.findById(id)
    }

    fun toPaymentStatusResponse(payment: Payment): PaymentStatusResponse {
        return payment.toStatusResponse()
    }

    fun verifyWebhookSignature(payload: String, signature: String?, timestamp: String? = null): Boolean {
        if (signature.isNullOrBlank()) return false

        try {
            val secretKey = cashfreeConfig.secretKey
            if (secretKey.isBlank()) {
                logger.warn("Cashfree secret key is empty")
                return false
            }

            val hmac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            hmac.init(secretKeySpec)

            // Cashfree PG standard: timestamp + payload
            if (!timestamp.isNullOrBlank()) {
                val dataToSign = "$timestamp$payload"
                val computedSigWithTimestamp = Base64.getEncoder().encodeToString(
                    hmac.doFinal(dataToSign.toByteArray(StandardCharsets.UTF_8))
                )
                if (computedSigWithTimestamp == signature) {
                    return true
                }
            }

            // Fallback: payload only
            val computedSigPayloadOnly = Base64.getEncoder().encodeToString(
                hmac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
            )
            return computedSigPayloadOnly == signature

        } catch (e: Exception) {
            logger.error("Error during webhook signature verification: {}", e.message)
            return false
        }
    }

    private fun extractOrderIdFromJson(payload: String): String? {
        return try {
            val tree = objectMapper.readTree(payload)
            tree.path("data").path("order").path("order_id").asText(null)
                ?: tree.path("order").path("order_id").asText(null)
                ?: tree.path("order_id").asText(null)
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePaymentMode(modeStr: String): com.iotabuild.zippyhand_Admin.order.entity.PaymentMode {
        val lower = modeStr.lowercase()
        return when {
            lower.contains("upi") -> com.iotabuild.zippyhand_Admin.order.entity.PaymentMode.UPI
            lower.contains("card") || lower.contains("debit") || lower.contains("credit") -> com.iotabuild.zippyhand_Admin.order.entity.PaymentMode.CARD
            lower.contains("netbanking") || lower.contains("nb") -> com.iotabuild.zippyhand_Admin.order.entity.PaymentMode.NET_BANKING
            lower.contains("cash") -> com.iotabuild.zippyhand_Admin.order.entity.PaymentMode.CASH
            else -> com.iotabuild.zippyhand_Admin.order.entity.PaymentMode.UPI
        }
    }

    private fun callCashfreeCreateOrder(request: CashfreeCreateOrderRequest): CashfreeOrderResponse {
        val url = "${cashfreeConfig.getBaseUrl()}$CREATE_ORDER_ENDPOINT"
        val jsonBody = objectMapper.writeValueAsString(request)

        val httpRequest = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .addHeader("x-client-id", cashfreeConfig.appId)
            .addHeader("x-client-secret", cashfreeConfig.secretKey)
            .addHeader("x-api-version", cashfreeConfig.apiVersion)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: throw CashfreeApiException("Empty response from Cashfree")

                if (!response.isSuccessful) {
                    throw CashfreeApiException("Failed to create order: ${response.code} - $responseBody")
                }

                return objectMapper.readValue(responseBody)
            }
        } catch (e: java.io.IOException) {
            throw CashfreeApiException("Network error while connecting to Cashfree: ${e.message}", e)
        }
    }

    fun callCashfreeGetPayments(cfOrderId: String): List<CashfreePaymentResponse> {
        val url = "${cashfreeConfig.getBaseUrl()}$GET_PAYMENT_ENDPOINT/$cfOrderId/payments"

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-client-id", cashfreeConfig.appId)
            .addHeader("x-client-secret", cashfreeConfig.secretKey)
            .addHeader("x-api-version", cashfreeConfig.apiVersion)
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: return emptyList()

                if (!response.isSuccessful) {
                    logger.warn("Failed to get payments for order {}: {} - {}", cfOrderId, response.code, responseBody)
                    return emptyList()
                }

                objectMapper.readValue<List<CashfreePaymentResponse>>(responseBody)
            }
        } catch (e: Exception) {
            logger.error("Error fetching payments from Cashfree for order {}: {}", cfOrderId, e.message)
            emptyList()
        }
    }

    fun callCashfreeGetOrder(cfOrderId: String): CashfreeOrderResponse {
        val url = "${cashfreeConfig.getBaseUrl()}$GET_ORDER_ENDPOINT/$cfOrderId"

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-client-id", cashfreeConfig.appId)
            .addHeader("x-client-secret", cashfreeConfig.secretKey)
            .addHeader("x-api-version", cashfreeConfig.apiVersion)
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: throw CashfreeApiException("Empty response from Cashfree")

            if (!response.isSuccessful) {
                throw CashfreeApiException("Failed to get order: ${response.code} - $responseBody")
            }

            return objectMapper.readValue(responseBody)
        }
    }

    private fun parsePaymentTime(timeString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    private fun generateOrderId(): Int {
        return Random.nextInt(10000, 100000)
    }
}
