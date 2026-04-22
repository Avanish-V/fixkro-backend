package com.iotabuild.zippyhand_Admin.order.service

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.service.AddressService
import com.iotabuild.zippyhand_Admin.payment.service.PaymentService
import com.iotabuild.zippyhand_Admin.pricing.service.PricingService
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import com.iotabuild.zippyhand_Admin.order.dto.*
import com.iotabuild.zippyhand_Admin.order.entity.*
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.pricing.dto.PriceRequest
import com.iotabuild.zippyhand_Admin.jobs.repository.JobAssignmentRepository
import com.iotabuild.zippyhand_Admin.notification.dto.SendNotificationRequest
import com.iotabuild.zippyhand_Admin.notification.model.DeliveryStrategy
import com.iotabuild.zippyhand_Admin.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class CustomerOrderService(
    private val orderRepository: OrderRepository,
    private val addressService: AddressService,
    private val paymentService: PaymentService,
    private val productRepository: ProductRepository,
    private val pricingService: PricingService,
    private val queryService: OrderQueryService,
    private val jobAssignmentRepository: JobAssignmentRepository,
    private val notificationService: NotificationService
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

        val product = productRepository.findById(request.productId)
            .orElseThrow { RuntimeException("Product not found") }

        val address = addressService.getAddressById(token, request.addressId)

        val paymentSession = paymentService.createCashfreeSession(
            orderId = cfOrderId,
            amount = pricingDetails.totalPrice.toDouble(),
            customerName = address.fullName,
            customerEmail = token.email ?: "customer@example.com",
            customerPhone = address.phone
        )

        val order = OrderEntity(
            customerId = token.uid,
            orderId = cfOrderId,
            totalAmount = pricingDetails.totalPrice.toInt(),
            productId = request.productId,
            productTitle = product.title,
            productImage = product.image,
            discount = pricingDetails.discount.toInt(),
            paymentStatus = PaymentStatus.PENDING,
            paymentMode = PaymentMode.UPI,
            scheduledAt = parseSchedule(request.serviceSchedule),
            price = pricingDetails.basePrice.toInt(),
            tax = pricingDetails.taxAmount.toInt(),
            offerCode = request.offerCode,
            fullName = address.fullName,
            phone = address.phone,
            addressLine1 = address.addressLine1,
            addressLine2 = address.addressLine2,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
        )

        val savedOrder = orderRepository.save(order)

        return CreateOrderResponse(
            orderId = savedOrder.orderId,
            cfOrderId = paymentSession.cfOrderId,
            paymentSessionId = paymentSession.paymentSessionId,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount
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
        val savedOrder = orderRepository.save(order)

        // Cancel all associated job assignments
        val assignments = jobAssignmentRepository.findAllByOrderId(order.orderId)
        assignments.forEach { assignment ->
            // Allow cancellation if the job hasn't been completed or already rejected/cancelled
            if (assignment.status == "PENDING" || assignment.status == "ACCEPTED" || assignment.status == "IN_PROGRESS") {
                val isUrgent = assignment.status != "PENDING" // They accepted it, meaning they might be traveling!
                assignment.status = "CANCELLED"
                jobAssignmentRepository.save(assignment)

                // Notify technician asynchronously to avoid blocking the customer cancellation flow
                CompletableFuture.runAsync {
                    try {
                        notificationService.enqueue(
                            SendNotificationRequest(
                                userId = assignment.uid,
                                idempotencyKey = "cancel_${order.orderId}_${assignment.uid}_${System.currentTimeMillis()}", // Break exact idempotency if retried to force delivery
                                strategy = if (isUrgent) DeliveryStrategy.MULTI_CHANNEL else DeliveryStrategy.SINGLE_CHANNEL, // Alert everywhere if urgent
                                templateId = "JOB_CANCELLED_BY_CUSTOMER",
                                payload = mapOf(
                                    "orderId" to order.orderId,
                                    "productTitle" to order.productTitle,
                                    "reason" to (reason ?: "User requested cancellation"),
                                    "urgent" to isUrgent
                                )
                            ),
                            // If urgent (already accepted), process it synchronously WITHIN this background thread
                            // to bypass Kafka lag and deliver it instantly via Firebase.
                            sync = isUrgent
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to notify technician ${assignment.uid} about cancellation of ${order.orderId}: ${e.message}")
                    }
                }
            }
        }
        
        return queryService.getOrder(savedOrder.id)
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
        // Fallback to now if parsing fails, but ideally we should throw an error
        return LocalDateTime.now().plusDays(1) 
    }
}
