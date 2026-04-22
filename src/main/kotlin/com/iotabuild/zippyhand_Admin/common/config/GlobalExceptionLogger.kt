package com.iotabuild.zippyhand_Admin.common.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import jakarta.servlet.http.HttpServletRequest

@ControllerAdvice
class GlobalExceptionLogger {
    private val logger = LoggerFactory.getLogger(GlobalExceptionLogger::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        logger.error("Exception occurred while processing request: ${request.requestURI}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            mapOf("error" to "Internal Server Error", "message" to (ex.message ?: "Unknown error"))
        )
    }
}
