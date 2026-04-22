package com.iotabuild.zippyhand_Admin.payment.exception

import com.iotabuild.zippyhand_Admin.common.exception.ApiException
import org.springframework.http.HttpStatus

open class PaymentException(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST) : ApiException(status, message, "PAYMENT_ERROR")

class CashfreeApiException(message: String, cause: Throwable? = null) : PaymentException(message)

class PaymentVerificationException(message: String) : PaymentException(message)

class PaymentNotFoundException(orderId: String) : PaymentException("Payment not found for order: $orderId", HttpStatus.NOT_FOUND)

class InvalidWebhookSignatureException : PaymentException("Invalid webhook signature", HttpStatus.UNAUTHORIZED)
