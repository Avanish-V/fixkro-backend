package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.AddressRepository
import com.iotabuild.zippyhand_Admin.address.AddressService
import com.iotabuild.zippyhand_Admin.common.exception.OrderNotFoundException
import com.iotabuild.zippyhand_Admin.payment.generateOrderId
import com.iotabuild.zippyhand_Admin.pricing.PricingService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressService,
    private val professionalRepository: com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository,
    private val couponService: com.iotabuild.zippyhand_Admin.coupon.service.CouponService,
    private val paymentService: com.iotabuild.zippyhand_Admin.payment.PaymentService,
    private val productRepository: com.iotabuild.zippyhand_Admin.products.repository.ProductRepository,
    private val pricingService: com.iotabuild.zippyhand_Admin.pricing.PricingService
) {


    private val logger = LoggerFactory.getLogger(PricingService::class.java)

    private fun entityToResponse(order: Order): OrderResponse {
        val technician = order.technicianId?.let { professionalRepository.findById(it).orElse(null) }
        
        return OrderResponse(
            id = order.id,
            orderId = order.orderId,
            customerId = order.customerId,
            productId = order.productId,
            productTitle = order.productTitle,
            productImage = order.productImage,
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
        val cfOrderId = "order_${generateOrderId()}_${System.currentTimeMillis()}"

        // 1. RE-CALCULATE AND VERIFY PRICING (Never trust client pricing for security)
        val pricingDetails = pricingService.calculateFinalPrice(
            com.iotabuild.zippyhand_Admin.pricing.PriceRequest(
                productId = request.productId,
                quantity = request.quantity,
                couponCode = request.couponCode
            )
        )

        val finalAmount = pricingDetails.totalPrice.toInt()
        val basePriceTotal = pricingDetails.basePrice.toInt()
        val verifiedTax = pricingDetails.taxAmount.toInt()
        val calculatedDiscount = pricingDetails.discount.toInt()
        
        val product = productRepository.findById(request.productId)
            .orElseThrow { RuntimeException("Product not found") }

        val address = addressRepository.getAddressById(token,request.addressId)

        logger.info("price-------------$finalAmount")

        // 2. Create Cashfree Session with VERIFIED amount
        val paymentSession = try {
            paymentService.createCashfreeSession(
                orderId = cfOrderId,
                amount = finalAmount.toDouble(),
                customerName = address.fullName,
                customerEmail = token.email ?: "customer@example.com",
                customerPhone = address.phone
            )
        } catch (e: Exception) {
            println("Failed to create Cashfree session: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Payment session creation failed: ${e.message}", e)
        }

        // 5. Save order to DB after successful session creation
        val order = Order(
            customerId = customerUid,
            orderId = cfOrderId,
            applianceBrand = "",
            applianceModel = "",
            totalAmount = finalAmount,
            productId = request.productId,
            productTitle = product.title,
            productImage = product.image,
            technicianId = null,
            discount = calculatedDiscount,
            paymentStatus = PaymentStatus.PENDING,
            paymentMode = PaymentMode.UPI,
            scheduledAt = request.serviceSchedule,
            price = basePriceTotal,
            tax = verifiedTax,
            couponCode = request.couponCode,
            fullName = address.fullName,
            phone = address.phone,
            addressLine1 = address.addressLine1,
            addressLine2 = address.addressLine2,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
        )

        val savedOrder = orderRepository.save(order)

        // Increment coupon usage if applied
        request.couponCode?.let { couponCode ->
            try {
                couponService.incrementUsageCount(couponCode)
            } catch (e: Exception) {
                // Log but don't fail order creation if coupon tracking fails
                println("Failed to increment coupon usage for $couponCode: ${e.message}")
            }
        }

        return CreateOrderResponse(
            orderId = savedOrder.orderId,
            cfOrderId = paymentSession.cfOrderId,
            paymentSessionId = paymentSession.paymentSessionId,
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
    fun getOrdersByCustomer(token: FirebaseToken?,pageable: Pageable): Page<OrderResponse> {
        return token?.let { orderRepository.findByCustomerId(it.uid,pageable) }
            ?.map { entityToResponse(it) } ?: Page.empty<OrderResponse>()
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

