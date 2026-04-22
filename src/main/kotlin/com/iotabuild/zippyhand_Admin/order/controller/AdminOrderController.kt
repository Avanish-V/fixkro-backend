package com.iotabuild.zippyhand_Admin.order.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import com.iotabuild.zippyhand_Admin.order.entity.OrderStatus
import com.iotabuild.zippyhand_Admin.order.service.AdminOrderService
import com.iotabuild.zippyhand_Admin.order.service.OrderQueryService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(*["/api/v1/admin/order", "/api/v1/order"])
class AdminOrderController(
    private val adminOrderService: AdminOrderService,
    private val queryService: OrderQueryService
) {

    @GetMapping("/all")
    fun getAllOrders(): ApiResponse<List<OrderResponse>> {
        return ApiResponse.success(queryService.getAllOrders())
    }

    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Long,
        @RequestParam status: OrderStatus
    ): ApiResponse<OrderResponse> {
        return ApiResponse.success(adminOrderService.updateOrderStatus(orderId, status))
    }

    @PostMapping("/{orderId}/assign-technician")
    fun assignTechnician(
        @PathVariable orderId: Long,
        @RequestParam technicianId: Long
    ): ApiResponse<OrderResponse> {
        return ApiResponse.success(adminOrderService.assignTechnician(orderId, technicianId))
    }

    @PostMapping("/{orderId}/unassign-technician")
    fun unassignTechnician(
        @PathVariable orderId: Long
    ): ApiResponse<OrderResponse> {
        return ApiResponse.success(adminOrderService.unassignTechnician(orderId))
    }
}
