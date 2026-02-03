package com.iotabuild.zippyhand_Admin.common.exception

import com.iotabuild.zippyhand_Admin.common.exception.ErrorCode
import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String,
    val code: String
) : RuntimeException(message) {

    companion object {

        fun badRequest(message: String) =
            ApiException(HttpStatus.BAD_REQUEST, message, ErrorCode.BAD_REQUEST)

        fun notFound(message: String) =
            ApiException(HttpStatus.NOT_FOUND, message, ErrorCode.NOT_FOUND)

        fun conflict(message: String) =
            ApiException(HttpStatus.CONFLICT, message, ErrorCode.CONFLICT)

        fun forbidden(message: String) =
            ApiException(HttpStatus.FORBIDDEN, message, ErrorCode.FORBIDDEN)

        fun internal(message: String) =
            ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message, ErrorCode.INTERNAL)
    }
}