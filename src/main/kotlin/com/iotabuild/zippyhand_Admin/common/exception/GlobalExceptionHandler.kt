package com.iotabuild.zippyhand_Admin.common.exception

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.hibernate.exception.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(ex.status)
            .body(ApiResponse.Companion.error(ex.message, ex.code))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException)
        : ResponseEntity<ApiResponse<Nothing>> {

        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.Companion.error(message, ErrorCode.BAD_REQUEST))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(ex: ConstraintViolationException)
        : ResponseEntity<ApiResponse<Nothing>> {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.Companion.error(ex.message ?: "Invalid request"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        ex.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.Companion.error("Something went wrong", ErrorCode.INTERNAL))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Companion.error(ex.message ?: "Resource not found"))
    }

}

class OrderNotFoundException(orderId: Long) :
    RuntimeException("Order not found with id=$orderId")
