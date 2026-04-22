package com.iotabuild.zippyhand_Admin.order.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.order.dto.*
import com.iotabuild.zippyhand_Admin.order.service.CustomerOrderService
import com.iotabuild.zippyhand_Admin.order.service.OrderQueryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/order")
class CustomerOrderController(
    private val customerOrderService: CustomerOrderService,
    private val queryService: OrderQueryService
) {

    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: OrderRequest
    ): ApiResponse<CreateOrderResponse> {
        return ApiResponse.success(customerOrderService.createOrder(token, request))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal token: FirebaseToken?,
        @PathVariable orderId: Long
    ): ApiResponse<OrderResponse> {
        // Validation of ownership happens in query service or controller if token exists
        val response = queryService.getOrder(orderId)
        if (token != null && response.customerId != token.uid) {
             throw RuntimeException("Access Denied")
        }
        return ApiResponse.success(response)
    }

    @GetMapping("/my-orders")
    fun getMyOrders(
        @AuthenticationPrincipal token: FirebaseToken,
        pageable: Pageable
    ): ApiResponse<Page<OrderResponse>> {
        return ApiResponse.success(queryService.getOrdersByCustomer(token.uid, pageable))
    }

    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal token: FirebaseToken,
        @PathVariable orderId: Long,
        @RequestParam(required = false) reason: String?
    ): ApiResponse<OrderResponse> {
        return ApiResponse.success(customerOrderService.cancelOrder(token, orderId, reason))
    }
}
