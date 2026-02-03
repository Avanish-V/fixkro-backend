package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/order")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: OrderRequest
    ): ResponseEntity<ApiResponse<CreateOrderResponse>> {
        val response = orderService.createOrder(token,request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @PathVariable orderId: Long
    ): ResponseEntity<OrderResponse> {

        val response = orderService.getOrder(orderId)
        return ResponseEntity.ok(response)
    }
}
