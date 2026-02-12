package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
        val response = orderService.createOrder(token, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/all")
    fun getAllOrders(): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val response = orderService.getAllOrders()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal token: FirebaseToken?,
        @PathVariable orderId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.getOrder(token, orderId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/my-orders")
    fun getMyOrders(
        @AuthenticationPrincipal token: FirebaseToken,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<OrderResponse>>> {
        val response = orderService.getOrdersByCustomer(token, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Long,
        @RequestParam status: OrderStatus
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        // In a real production app, this would be restricted to ADMIN role
        val response = orderService.updateOrderStatus(orderId, status)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal token: FirebaseToken?,
        @PathVariable orderId: Long,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.cancelOrder(token, orderId, reason)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/{orderId}/assign-technician")
    fun assignTechnician(
        @PathVariable orderId: Long,
        @RequestParam technicianId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.assignTechnician(orderId, technicianId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
