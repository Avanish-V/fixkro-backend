package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.exception.OrderNotFoundException
import com.iotabuild.zippyhand_Admin.payment.generateOrderId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val professionalRepository: com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
) {

    private fun entityToResponse(order: Order): OrderResponse {
        val technician = order.technicianId?.let { professionalRepository.findById(it).orElse(null) }
        
        return OrderResponse(
            id = order.id,
            orderId = order.orderId,
            customerId = order.customerId,
            productId = order.productId,
            productTitle = order.productTitle,
            applianceBrand = order.applianceBrand,
            applianceModel = order.applianceModel,
            technicianId = order.technicianId,
            technicianName = technician?.name,
            technicianPhoto = technician?.photo,
            price = order.price,
            tax = order.tax,
            offerId = order.offerId,
            couponCode = order.couponCode,
            discount = order.discount,
            totalAmount = order.totalAmount,
            status = order.status,
            paymentStatus = order.paymentStatus,
            paymentMode = order.paymentMode,
            scheduledAt = order.scheduledAt,
            completedAt = order.completedAt,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            cancellationReason = order.cancellationReason,
            serviceAddress = Address(
                fullName = order.fullName,
                phone = order.phone,
                addressLine1 = order.addressLine1,
                addressLine2 = order.addressLine2,
                city = order.city,
                state = order.state,
                postalCode = order.postalCode,
            )
        )
    }

    @Transactional
    fun createOrder(token: FirebaseToken, request: OrderRequest): CreateOrderResponse {

        val customerUid = token.uid
        val orderId = "order_${generateOrderId()}_${System.currentTimeMillis()}"

        val order = Order(
            customerId = customerUid,
            orderId = orderId,
            applianceBrand = request.serviceProduct.applianceBrand,
            applianceModel = request.serviceProduct.applianceModel,
            totalAmount = request.priceDetail.totalAmount,
            productId = request.serviceProduct.productId.toLong(),
            productTitle = request.serviceProduct.productTitle,
            technicianId = request.technicianId?.toLong(),
            discount = request.priceDetail.discount,
            paymentStatus = PaymentStatus.PAID,
            paymentMode = PaymentMode.UPI,
            scheduledAt = request.serviceSchedule,
            price = request.priceDetail.price,
            tax = request.priceDetail.tax,
            couponCode = request.priceDetail.coupon,
            fullName = request.serviceAddress.fullName,
            phone = request.serviceAddress.phone,
            addressLine1 = request.serviceAddress.addressLine1,
            addressLine2 = request.serviceAddress.addressLine2,
            city = request.serviceAddress.city,
            state = request.serviceAddress.state,
            postalCode =request.serviceAddress.postalCode,
        )

        val savedOrder = orderRepository.save(order)

        return CreateOrderResponse(
            orderId = savedOrder.id,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount
        )
    }

    @Transactional(readOnly = true)
    fun getOrder(token: FirebaseToken?, orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }

        // Security check: Only owner or admin can see the order
        // If token is null, we assume it's an internal/admin request for now (Dev Mode)
        if (token != null && order.customerId != token.uid) {
            // Assuming we check for ADMIN role if needed, but for now simple owner check
            throw AccessDeniedException("You do not have permission to view this order")
        }

        return entityToResponse(order)
    }

    @Transactional(readOnly = true)
    fun getOrdersByCustomer(token: FirebaseToken, pageable: Pageable): Page<OrderResponse> {
        return orderRepository.findByCustomerId(token.uid, pageable)
            .map { entityToResponse(it) }
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        if (!order.canTransitionTo(newStatus)) {
            throw IllegalStateException("Cannot transition order from ${order.status} to $newStatus")
        }

        order.status = newStatus
        if (newStatus == OrderStatus.COMPLETED) {
            order.completedAt = java.time.LocalDateTime.now()
        }

        return entityToResponse(orderRepository.save(order))
    }

    @Transactional
    fun cancelOrder(token: FirebaseToken?, orderId: Long, reason: String?): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // Security check
        if (token != null && order.customerId != token.uid) {
            throw AccessDeniedException("You do not have permission to cancel this order")
        }

        if (!order.canTransitionTo(OrderStatus.CANCELLED)) {
            throw IllegalStateException("Order cannot be cancelled in its current state: ${order.status}")
        }

        order.status = OrderStatus.CANCELLED
        order.cancellationReason = reason
        
        return entityToResponse(orderRepository.save(order))
    }

    @Transactional
    fun assignTechnician(orderId: Long, technicianId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        order.technicianId = technicianId
        
        // Auto-transition to ASSIGNED if it's in early stages
        if (order.status == OrderStatus.CREATED) {
            order.status = OrderStatus.ASSIGNED
        }

        return entityToResponse(orderRepository.save(order))
    }

    @Transactional
    fun updateOrderPaymentStatus(orderIdStr: String, paymentStatus: PaymentStatus) {
        val order = orderRepository.findByOrderId(orderIdStr)
            .orElseThrow { RuntimeException("Order not found with orderId: $orderIdStr") }

        order.paymentStatus = paymentStatus
        orderRepository.save(order)
    }

    @Transactional(readOnly = true)
    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll().map { entityToResponse(it) }
    }

}

