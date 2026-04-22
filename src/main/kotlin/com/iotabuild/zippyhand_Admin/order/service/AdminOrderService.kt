package com.iotabuild.zippyhand_Admin.order.service

import com.iotabuild.inAppOTP.OtpService
import com.iotabuild.zippyhand_Admin.jobs.entity.JobAssignment
import com.iotabuild.zippyhand_Admin.jobs.repository.JobAssignmentRepository
import com.iotabuild.zippyhand_Admin.notification.dto.SendNotificationRequest
import com.iotabuild.zippyhand_Admin.notification.model.DeliveryStrategy
import com.iotabuild.zippyhand_Admin.notification.service.NotificationService
import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import com.iotabuild.zippyhand_Admin.order.entity.OrderStatus
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminOrderService(
    private val orderRepository: OrderRepository,
    private val professionalRepository: ProfessionalRepository,
    private val jobAssignmentRepository: JobAssignmentRepository,
    private val otpService: OtpService,
    private val notificationService: NotificationService,
    private val queryService: OrderQueryService
) {
    private val logger = LoggerFactory.getLogger(AdminOrderService::class.java)

    @Transactional
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        if (!order.canTransitionTo(newStatus)) {
            throw IllegalStateException("Cannot transition order from ${order.status} to $newStatus")
        }
        order.status = newStatus
        if (newStatus == OrderStatus.COMPLETED) {
            order.completedAt = LocalDateTime.now()
        } else if (newStatus == OrderStatus.CANCELLED) {
            val assignments = jobAssignmentRepository.findAllByOrderId(order.orderId)
            assignments.forEach { assignment ->
                if (assignment.status == "PENDING" || assignment.status == "ACCEPTED") {
                    assignment.status = "CANCELLED"
                    jobAssignmentRepository.save(assignment)

                    try {
                        notificationService.enqueue(
                            SendNotificationRequest(
                                userId = assignment.uid,
                                idempotencyKey = "admin_cancel_${order.orderId}_${assignment.uid}",
                                strategy = DeliveryStrategy.SINGLE_CHANNEL,
                                templateId = "JOB_CANCELLED_BY_ADMIN",
                                payload = mapOf(
                                    "orderId" to order.orderId,
                                    "productTitle" to order.productTitle,
                                    "reason" to "Order cancelled by administrator"
                                )
                            ),
                            sync = false
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to notify technician ${assignment.uid} about admin cancellation of ${order.orderId}: ${e.message}")
                    }
                }
            }
        }
        return queryService.getOrder(orderRepository.save(order).id)
    }

    @Transactional
    fun assignTechnician(orderId: Long, technicianId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        val technician = professionalRepository.findById(technicianId)
            .orElseThrow { RuntimeException("Technician not found with ID: $technicianId") }

        logger.info("Assigning technician {} to order {}", technicianId, order.orderId)
        
        try {
            otpService.generateAndSaveOtp(order.orderId)
        } catch (e: Exception) {
            logger.error("Failed to generate OTP for order ${order.orderId}: ${e.message}")
            // Decide if you want to fail assignment if OTP fails. Usually yes for production.
        }

        val jobAssignment = JobAssignment(
            orderId = order.orderId,
            professionalId = technicianId,
            uid = technician.firebaseUid,
            status = "PENDING",
            assignedAt = LocalDateTime.now()
        )
        jobAssignmentRepository.save(jobAssignment)

        if (order.status == OrderStatus.CREATED) {
            order.status = OrderStatus.ASSIGNED
        }

        try {
            val scheduledTimeStr = order.scheduledAt?.toString() ?: "Not Scheduled"
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
                        "scheduledAt" to scheduledTimeStr,
                        "customerName" to order.fullName,
                        "address" to "${order.addressLine1}, ${order.city}"
                    )
                ),
                sync = true
            )
        } catch (e: Exception) {
            logger.error("Failed to enqueue notification for order ${order.orderId}: ${e.message}")
        }
        
        return queryService.getOrder(orderRepository.save(order).id)
    }

    @Transactional
    fun unassignTechnician(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        otpService.deleteOtp(order.orderId)
        
        val assignments = jobAssignmentRepository.findAllByOrderId(order.orderId)
            .filter { it.status !in listOf("CANCELLED", "REJECTED") }
            
        jobAssignmentRepository.deleteAll(assignments)

        if (order.status == OrderStatus.ASSIGNED) {
            order.status = OrderStatus.CREATED
        }
        
        return queryService.getOrder(orderRepository.save(order).id)
    }
}
