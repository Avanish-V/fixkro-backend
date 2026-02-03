package com.iotabuild.zippyhand_Admin.payment

import com.iotabuild.zippyhand_Admin.common.exception.ApiException
import com.iotabuild.zippyhand_Admin.common.exception.ErrorCode
import org.springframework.http.HttpStatus

class PaymentNotFoundException(orderId: String) :
    ApiException(HttpStatus.NOT_FOUND, "Payment not found for order ID: $orderId", ErrorCode.NOT_FOUND)

class PaymentAlreadyExistsException(orderId: Long) :
    ApiException(HttpStatus.CONFLICT, "Payment already exists for order ID: $orderId", ErrorCode.CONFLICT)

class PaymentFailedException(message: String) :
    ApiException(HttpStatus.BAD_REQUEST, "Payment failed: $message", ErrorCode.BAD_REQUEST)

class PaymentVerificationException(message: String) :
    ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment verification failed: $message", ErrorCode.INTERNAL)

class CashfreeApiException(message: String, cause: Throwable? = null) :
    RuntimeException("Cashfree API Error: $message", cause)

class InvalidWebhookSignatureException(message: String = "Invalid webhook signature") :
    ApiException(HttpStatus.FORBIDDEN, message, ErrorCode.FORBIDDEN)
