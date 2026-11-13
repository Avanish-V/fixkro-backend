package com.iotabuild.zippyhand_Admin.order.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.service.AddressService
import com.iotabuild.zippyhand_Admin.jobs.service.PartnerJobService
import com.iotabuild.zippyhand_Admin.order.dto.*
import com.iotabuild.zippyhand_Admin.order.entity.*
import com.iotabuild.zippyhand_Admin.order.repository.OrderPaymentRepository
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.payment.service.PaymentService
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.service.PricingService
import com.iotabuild.zippyhand_Admin.products.service.ProductQueryService
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Service
class CustomerOrderService(
    private val orderRepository: OrderRepository,
    private val orderPaymentRepository: OrderPaymentRepository,
    private val addressService: AddressService,
    private val paymentService: PaymentService,
    private val productQueryService: ProductQueryService,
    private val pricingService: PricingService,
    private val queryService: OrderQueryService,
    private val partnerJobService: PartnerJobService
) {
    private val logger = LoggerFactory.getLogger(CustomerOrderService::class.java)

    @Transactional
    fun createOrder(token: FirebaseToken, request: OrderRequest): CreateOrderResponse {
        // Generate a short, readable, sortable Order ID: ORD-YYMMDD-XXXX
        val now = LocalDateTime.now()
        val datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"))
        val randomPart = (1..4).map { Random.nextInt(0, 36).let { if (it < 10) '0' + it else 'A' + (it - 10) } }.joinToString("")
        val cfOrderId = "ORD${datePart}${randomPart}"

        val pricingDetails = pricingService.calculateFinalPrice(
            PriceRequest(
                productId = request.productId,
                quantity = request.quantity,
                offerCode = request.offerCode
            )
        )

        val product = productQueryService.getProduct(request.productId)
        val address = addressService.getAddressById(token, request.addressId)
        val paymentMode = request.paymentMode ?: PaymentMode.UPI

        // 1. Create payment gateway session with Cashfree
        val paymentSession = paymentService.createCashfreeSession(
            orderId = cfOrderId,
            amount = pricingDetails.totalPrice.toDouble(),
            customerName = address.fullName,
            customerEmail = token.email ?: "customer@example.com",
            customerPhone = address.phone,
            paymentMode = paymentMode.name
        )

        // 2. Maintain separate orders_payment entity storing all payment information
        val orderPayment = OrderPayment(
            orderId = cfOrderId,
            cfOrderId = paymentSession.cfOrderId,
            paymentSessionId = paymentSession.paymentSessionId,
            amount = pricingDetails.totalPrice.toInt(),
            currency = paymentSession.currency,
            paymentStatus = PaymentStatus.PENDING,
            paymentMode = paymentMode,
            customerName = address.fullName,
            customerPhone = address.phone,
            customerEmail = token.email ?: "customer@example.com",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // 3. Maintain Orders table and map that order with payment information via @OneToOne
        val order = OrderEntity(
            customerId = token.uid,
            orderId = cfOrderId,
            payment = orderPayment, // Maps to orders_payment table
            paymentStatus = PaymentStatus.PENDING,
            paymentMode = paymentMode,
            totalAmount = pricingDetails.totalPrice.toInt(),
            productId = request.productId,
            productTitle = product.title,
            productImage = product.imageUrl,
            discount = pricingDetails.discount.toInt(),
            scheduledAt = parseSchedule(request.serviceSchedule),
            price = pricingDetails.basePrice.toInt(),
            tax = pricingDetails.taxAmount.toInt(),
            offerCode = request.offerCode,
            fullName = address.fullName,
            phone = address.phone,
            addressLine1 = address.addressLine1,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
        )

        val savedOrder = orderRepository.save(order)
        val savedPayment = savedOrder.payment

        logger.info("Order {} and mapped orders_payment {} created successfully", savedOrder.orderId, savedPayment?.id)

        return CreateOrderResponse(
            orderId = savedOrder.orderId,
            cfOrderId = paymentSession.cfOrderId,
            paymentSessionId = paymentSession.paymentSessionId,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount,
            paymentId = savedPayment?.id,
            payment = savedPayment?.toResponse()
        )
    }

    @Transactional
    fun cancelOrder(token: FirebaseToken, orderId: Long, reason: String?): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }

        if (order.customerId != token.uid) {
            throw AccessDeniedException("You do not have permission to cancel this order")
        }

        if (!order.canTransitionTo(OrderStatus.CANCELLED)) {
            throw IllegalStateException("Order cannot be cancelled in its current state")
        }

        order.status = OrderStatus.CANCELLED
        order.cancellationReason = reason
        order.payment?.let { payment ->
            if (payment.paymentStatus == PaymentStatus.PENDING) {
                payment.paymentStatus = PaymentStatus.CANCELLED
                payment.updatedAt = LocalDateTime.now()
            }
        }
        val savedOrder = orderRepository.save(order)

        // Delegate job assignment cancellation and notification to PartnerJobService
        partnerJobService.cancelAssignmentsForOrder(
            orderId = order.orderId,
            reason = reason,
            isCustomerCancellation = true,
            productTitle = order.productTitle
        )
        
        return queryService.getOrder(savedOrder.id)
    }

    @Transactional(readOnly = true)
    fun getOrderPayment(token: FirebaseToken, orderId: Long): OrderPaymentResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        if (order.customerId != token.uid) {
            throw AccessDeniedException("Access Denied")
        }
        val payment = order.payment
            ?: orderPaymentRepository.findByOrderId(order.orderId)
                .orElseThrow { RuntimeException("Payment information not found in orders_payment for order: ${order.orderId}") }
        return payment.toResponse()
    }

    @Transactional(readOnly = true)
    fun checkOrderPaymentStatus(token: FirebaseToken, orderId: Long): com.iotabuild.zippyhand_Admin.payment.dto.PaymentCheckStatusResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        if (order.customerId != token.uid) {
            throw AccessDeniedException("Access Denied")
        }
        return paymentService.checkPaymentStatus(order.orderId)
    }

    private fun parseSchedule(scheduleStr: String): LocalDateTime {
        val cleanSchedule = scheduleStr.trim().replace("\\s+".toRegex(), " ")
        val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(cleanSchedule, formatter)
            } catch (e: Exception) {
                continue
            }
        }
        
        logger.error("Failed to parse schedule date: {}", scheduleStr)
        return LocalDateTime.now().plusDays(1) 
    }
}
