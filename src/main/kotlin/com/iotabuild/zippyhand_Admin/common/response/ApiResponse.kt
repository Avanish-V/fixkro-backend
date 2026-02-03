package com.iotabuild.zippyhand_Admin.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
) {
    companion object {

        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        fun error(message: String, code: String? = null): ApiResponse<Nothing> =
            ApiResponse(
                success = false,
                error = ApiError(message, code)
            )
    }
}

data class ApiError(
    val message: String,
    val code: String?
)
