package com.iotabuild.zippyhand_Admin.jobs.service

import com.iotabuild.inAppOTP.OtpService
import com.iotabuild.zippyhand_Admin.jobs.dto.*
import com.iotabuild.zippyhand_Admin.jobs.entity.JobAssignment
import com.iotabuild.zippyhand_Admin.jobs.repository.JobAssignmentRepository
import com.iotabuild.zippyhand_Admin.notification.dto.SendNotificationRequest
import com.iotabuild.zippyhand_Admin.notification.model.DeliveryStrategy
import com.iotabuild.zippyhand_Admin.notification.service.NotificationService
import com.iotabuild.zippyhand_Admin.order.service.OrderLifecycleService
import com.iotabuild.zippyhand_Admin.wallet.service.WalletService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Service
class PartnerJobService(
    private val repository: JobAssignmentRepository,
    private val orderLifecycleService: OrderLifecycleService,
    private val otpService: OtpService,
    private val walletService: WalletService,
    private val passwordEncoder: PasswordEncoder,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(PartnerJobService::class.java)

    fun getAssignedJobs(uid: String): List<JobListItemResponse> {
        return repository.findByUid(uid)
            .filter { it.status != "CANCELLED" || it.acceptedAt != null }
            .map { toListItemResponse(it) }
    }

    fun getPendingJobs(uid: String): List<JobListItemResponse> {
        return repository.findByUidAndStatus(uid, "PENDING").map { toListItemResponse(it) }
    }

    fun getCompletedJobs(uid: String): List<JobListItemResponse> {
        return repository.findByUidAndStatus(uid, "COMPLETED").map { toListItemResponse(it) }
    }
    
    fun getJobDetails(id: Long, uid: String): JobDetailsResponse {
        val assignment = repository.findById(id).orElseThrow { RuntimeException("Assignment not found") }
        if (assignment.uid != uid) throw RuntimeException("Unauthorized to view this job")
        return toDetailsResponse(assignment)
    }

    @Transactional
    fun acceptJob(assignmentId: Long, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { RuntimeException("Assignment not found") }
        
        if (assignment.uid != uid) throw RuntimeException("Unauthorized to perform this action")
        
        if (assignment.status == "CANCELLED") {
            throw IllegalStateException("This booking has been cancelled by the customer.")
        }
        if (assignment.status != "PENDING") {
            throw IllegalStateException("This booking is no longer pending (Current status: ${assignment.status}).")
        }

        // Double check base order as a fail-safe
        if (orderLifecycleService.isOrderCancelled(assignment.orderId)) {
            throw IllegalStateException("This booking has been cancelled by the customer.")
        }

        assignment.status = "ACCEPTED"
        assignment.acceptedAt = LocalDateTime.now()

        orderLifecycleService.markOrderInProgress(assignment.orderId)

        return toDetailsResponse(repository.save(assignment))
    }

    @Transactional
    fun rejectJob(assignmentId: Long, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { RuntimeException("Assignment not found") }
        
        if (assignment.uid != uid) throw RuntimeException("Unauthorized to perform this action")
        
        if (assignment.status == "CANCELLED") {
            throw IllegalStateException("This booking has been cancelled by the customer.")
        }
        if (assignment.status != "PENDING") {
            throw IllegalStateException("This booking is no longer pending (Current status: ${assignment.status}).")
        }

        assignment.status = "REJECTED"
        return toDetailsResponse(repository.save(assignment))
    }

    fun sendCompletionOtp(assignmentId: Long, uid: String): String {
        val assignment = repository.findById(assignmentId).orElseThrow { RuntimeException("Assignment not found") }
        if (assignment.uid != uid) throw RuntimeException("Unauthorized to perform this action")
        
        return "Please ask the customer for the OTP displayed in their Zippyhand mobile app."
    }

    @Transactional
    fun completeJob(assignmentId: Long, otp: String, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { RuntimeException("Assignment not found") }
        if (assignment.uid != uid) throw RuntimeException("Unauthorized to perform this action")
        if (assignment.status == "COMPLETED") throw RuntimeException("Job is already completed")
        
        val order = orderLifecycleService.findByOrderId(assignment.orderId)
            ?: throw RuntimeException("Order not found")

        val otpRecord = otpService.getOtpRecord(order.orderId)
            ?: throw RuntimeException("No active OTP found")

        val isOtpValid = passwordEncoder.matches(otp, otpRecord.otpHash)
        if (!isOtpValid) {
            logger.warn("Invalid OTP attempt for order ${order.orderId}")
            throw RuntimeException("Invalid OTP provided")
        }
            
        assignment.status = "COMPLETED"
        assignment.completedAt = LocalDateTime.now()
        repository.save(assignment)

        orderLifecycleService.markOrderCompleted(order.orderId)

        val totalPrice = BigDecimal(order.totalAmount)
        val commission = BigDecimal.ZERO
        walletService.recordJobEarning(uid, order.orderId, order.productTitle, totalPrice, commission)
        
        logger.info("Job completed successfully for order ${order.orderId}")
        
        return toDetailsResponse(assignment)
    }

    @Transactional
    fun assignJob(orderId: String, technicianId: Long, uid: String): JobAssignment {
        val assignment = JobAssignment(
            orderId = orderId,
            professionalId = technicianId,
            uid = uid,
            status = "PENDING",
            assignedAt = LocalDateTime.now()
        )
        return repository.save(assignment)
    }

    @Transactional
    fun unassignTechnicians(orderId: String) {
        val assignments = repository.findAllByOrderId(orderId)
            .filter { it.status !in listOf("CANCELLED", "REJECTED") }
        repository.deleteAll(assignments)
    }

    @Transactional
    fun cancelAssignmentsForOrder(
        orderId: String,
        reason: String?,
        isCustomerCancellation: Boolean,
        productTitle: String?
    ) {
        val assignments = repository.findAllByOrderId(orderId)
        assignments.forEach { assignment ->
            val isEligible = if (isCustomerCancellation) {
                assignment.status in listOf("PENDING", "ACCEPTED", "IN_PROGRESS")
            } else {
                assignment.status in listOf("PENDING", "ACCEPTED")
            }

            if (isEligible) {
                val isUrgent = assignment.status != "PENDING"
                assignment.status = "CANCELLED"
                repository.save(assignment)

                val templateId = if (isCustomerCancellation) "JOB_CANCELLED_BY_CUSTOMER" else "JOB_CANCELLED_BY_ADMIN"
                val defaultReason = if (isCustomerCancellation) "User requested cancellation" else "Order cancelled by administrator"

                CompletableFuture.runAsync {
                    try {
                        notificationService.enqueue(
                            SendNotificationRequest(
                                userId = assignment.uid,
                                idempotencyKey = "cancel_${orderId}_${assignment.uid}_${System.currentTimeMillis()}",
                                strategy = if (isUrgent) DeliveryStrategy.MULTI_CHANNEL else DeliveryStrategy.SINGLE_CHANNEL,
                                templateId = templateId,
                                payload = mapOf(
                                    "orderId" to orderId,
                                    "productTitle" to (productTitle ?: "Service"),
                                    "reason" to (reason ?: defaultReason),
                                    "urgent" to isUrgent
                                )
                            ),
                            sync = isUrgent
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to notify technician ${assignment.uid} about cancellation of $orderId: ${e.message}")
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    fun getActiveAssignmentForOrder(orderId: String): JobAssignment? {
        return repository.findAllByOrderId(orderId)
            .filter { it.status !in listOf("REJECTED", "CANCELLED") }
            .maxByOrNull { it.assignedAt }
    }

    private fun toListItemResponse(assignment: JobAssignment): JobListItemResponse {
        val order = orderLifecycleService.findByOrderId(assignment.orderId)
        return JobListItemResponse(
            id = assignment.id,
            orderId = assignment.orderId,
            partnerId = assignment.uid,
            status = assignment.status,
            assignedAt = assignment.assignedAt,
            acceptedAt = assignment.acceptedAt,
            productTitle = order?.productTitle,
            scheduledAt = order?.scheduledAt,
            addressLine1 = order?.addressLine1,
            city = order?.city,
            customerName = order?.fullName,
            customerPhone = order?.phone,
            totalPrice = order?.totalAmount
        )
    }

    private fun toDetailsResponse(assignment: JobAssignment): JobDetailsResponse {
        val order = orderLifecycleService.findByOrderId(assignment.orderId)
        val totalPrice = order?.totalAmount ?: 0
        val commission = 0
        val payout = totalPrice

        return JobDetailsResponse(
            id = assignment.id,
            orderId = assignment.orderId,
            partnerId = assignment.uid,
            status = assignment.status,
            assignedAt = assignment.assignedAt,
            acceptedAt = assignment.acceptedAt,
            customerName = order?.fullName,
            customerPhone = order?.phone,
            addressLine1 = order?.addressLine1,
            addressLine2 = order?.addressLine2,
            city = order?.city,
            state = order?.state,
            postalCode = order?.postalCode,
            applianceBrand = order?.applianceBrand,
            applianceModel = order?.applianceModel,
            productTitle = order?.productTitle,
            scheduledAt = order?.scheduledAt,
            price = order?.price,
            tax = order?.tax,
            discount = order?.discount,
            totalPrice = totalPrice,
            commission = commission,
            payout = payout
        )
    }
}
