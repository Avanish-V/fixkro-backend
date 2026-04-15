package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.inAppOTP.OtpService
import com.iotabuild.zippyhand_Admin.address.AddressService
import com.iotabuild.zippyhand_Admin.common.exception.OrderNotFoundException
import com.iotabuild.zippyhand_Admin.coupon.service.CouponService
import com.iotabuild.zippyhand_Admin.payment.PaymentService
import com.iotabuild.zippyhand_Admin.payment.generateOrderId
import com.iotabuild.zippyhand_Admin.pricing.PriceRequest
import com.iotabuild.zippyhand_Admin.pricing.PricingService
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import com.iotabuild.zippyhand_Admin.professionals.models.ProfessionalEntity
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import com.iotabuild.zippyhand_Admin.notification.dto.SendNotificationRequest
import com.iotabuild.zippyhand_Admin.notification.model.DeliveryStrategy
import com.iotabuild.zippyhand_Admin.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val addressService: AddressService,
    private val professionalRepository: ProfessionalRepository,
    private val couponService: CouponService,
    private val paymentService: PaymentService,
    private val productRepository: ProductRepository,
    private val pricingService: PricingService,
    private val jobAssigningRepository: JobAssigningRepository,
    private val notificationService: NotificationService,
    private val otpService: OtpService
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    /**
     * Maps an Order entity to OrderResponse, stitching data from the latest valid JobAssigning record.
     */
    private fun entityToResponse(order: Order): OrderResponse {
        val latestAssignment = jobAssigningRepository.findByOrderId(order.orderId)
            .filter { it.status !in listOf("REJECTED", "CANCELLED") }
            .maxByOrNull { it.assignedAt }

        // PRODUCTION RULE: Check if the current user is an Admin
        val isAdmin = SecurityContextHolder.getContext().authentication?.authorities
            ?.contains(SimpleGrantedAuthority("ROLE_ADMIN")) == true

        // PRODUCTION RULE:
        // 1. Admin always sees technician identity.
        // 2. Customers only see technician identity (Name, Photo, etc.) once they have ACCEPTED.
        val showTechIdentity = isAdmin || latestAssignment?.status in listOf("ACCEPTED", "COMPLETED")
        
        val technician = if (showTechIdentity && latestAssignment != null) {
            professionalRepository.findById(latestAssignment.technicianId).orElse(null)
        } else null

        // OTP should only be visible to the customer AFTER the technician has ACCEPTED the job
        val decryptedOtp = if (showTechIdentity && latestAssignment != null) {
            otpService.decryptOtpForCustomer(order.orderId)
        } else null
        
        return OrderResponse(
            id = order.id,
            orderId = order.orderId,
            customerId = order.customerId,
            productId = order.productId,
            productTitle = order.productTitle,
            productImage = order.productImage,
            applianceBrand = order.applianceBrand,
            applianceModel = order.applianceModel,
            technicianId = technician?.id,
            technicianName = technician?.name,
            technicianPhoto = technician?.photo,
            technicianUid = technician?.firebaseUid,
            technicianPhone = technician?.mobile,
            otp = decryptedOtp,
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
            PriceRequest(
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

        val address = addressService.getAddressById(token, request.addressId)

        logger.info("Verifying order price: $finalAmount")

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
            logger.error("Failed to create Cashfree session: ${e.message}", e)
            throw RuntimeException("Payment session creation failed: ${e.message}", e)
        }

        // 3. Save order to DB after successful session creation
        val order = Order(
            customerId = customerUid,
            orderId = cfOrderId,
            applianceBrand = "",
            applianceModel = "",
            totalAmount = finalAmount,
            productId = request.productId,
            productTitle = product.title,
            productImage = product.image,
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
                logger.warn("Failed to increment usage for coupon $couponCode: ${e.message}")
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
        val order = try {
            orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        } catch (e: Exception) {
            logger.error("JDBC Error while loading order by ID $orderId: ${e.message}", e)
            throw e
        }

        if (token != null && order.customerId != token.uid) {
            throw AccessDeniedException("You do not have permission to view this order")
        }

        return entityToResponse(order)
    }

    @Transactional(readOnly = true)
    fun getOrdersByCustomer(token: FirebaseToken?, pageable: Pageable): Page<OrderResponse> {
        return try {
            token?.let { tokenVal ->
                orderRepository.findByCustomerId(tokenVal.uid, pageable)
                    .map { entityToResponse(it) }
            } ?: Page.empty()
        } catch (e: Exception) {
            logger.error("JDBC Error while loading orders for customer ${token?.uid}: ${e.message}", e)
            throw e
        }
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
            order.completedAt = LocalDateTime.now()
        }

        return entityToResponse(orderRepository.save(order))
    }

    @Transactional
    fun cancelOrder(token: FirebaseToken?, orderId: Long, reason: String?): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

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
        if (!professionalRepository.existsById(technicianId)) {
            throw RuntimeException("Technician not found with ID: $technicianId")
        }
        return assignJob(orderId, technicianId)
    }

    /**
     * Internal logic to handle technician assignment.
     */
    private fun assignJob(orderId: Long, technicianId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // Fetch technician to get their firebaseUid
        val technician = professionalRepository.findById(technicianId)
            .orElseThrow { RuntimeException("Technician not found with ID: $technicianId") }

        // Generate and save OTP to the separate table
        otpService.generateAndSaveOtp(order.orderId)

        // 1. Create a record in job_assigning table (OTP info now stored separately)
        val jobAssigning = JobAssigning(
            orderId = order.orderId,
            technicianId = technicianId,
            uid = technician.firebaseUid,
            status = "PENDING",
            assignedAt = LocalDateTime.now()
        )
        jobAssigningRepository.save(jobAssigning)

        // 2. Update the Order status if needed
        if (order.status == OrderStatus.CREATED) {
            order.status = OrderStatus.ASSIGNED
        }

        // 3. Send FCM Notification to Technician
        try {
            logger.info("OrderService: Attempting to notify technician UID: '{}'", technician.firebaseUid)
            notificationService.enqueue(
                SendNotificationRequest(
                    userId = technician.firebaseUid,
                    idempotencyKey = "assignment_${order.orderId}_$technicianId",
                    strategy = DeliveryStrategy.SINGLE_CHANNEL,
                    templateId = "NEW_JOB_ASSIGNED",
                    payload = mapOf(
                        "orderId" to order.orderId,
                        "technicianName" to technician.name,
                        "productTitle" to order.productTitle,
                        "scheduledAt" to order.scheduledAt.toString(),
                        "customerName" to order.fullName,
                        "address" to "${order.addressLine1}, ${order.city}"
                    )
                ),
                sync = true
            )
            logger.info("Notification enqueued for technician ${technician.firebaseUid} for order ${order.orderId}")
        } catch (e: Exception) {
            logger.error("Failed to enqueue notification for job assignment: ${e.message}")
        }
        
        return entityToResponse(orderRepository.save(order))
    }

    @Transactional
    fun unassignTechnician(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // 0. Delete associated OTP
        otpService.deleteOtp(order.orderId)

        // 1. Clear active assignments specifically for this order
        val assignments = jobAssigningRepository.findByOrderId(order.orderId)
            .filter { it.status != "CANCELLED" && it.status != "REJECTED" }
            
        // Optional: Instead of deleteAll, maybe mark as CANCELLED for history?
        // jobAssigningRepository.saveAll(assignments.onEach { it.status = "CANCELLED" })
        jobAssigningRepository.deleteAll(assignments)

        // 2. Revert order status if necessary
        if (order.status == OrderStatus.ASSIGNED) {
            order.status = OrderStatus.CREATED
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
        return try {
            orderRepository.findAll().map(::entityToResponse)
        } catch (e: Exception) {
            logger.error("JDBC Error while loading all orders: ${e.message}", e)
            throw e
        }
    }
}
