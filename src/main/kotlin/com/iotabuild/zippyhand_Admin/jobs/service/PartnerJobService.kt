package com.iotabuild.zippyhand_Admin.jobs.service

import com.iotabuild.zippyhand_Admin.jobs.dto.*
import com.iotabuild.zippyhand_Admin.jobs.entity.JobAssignment
import com.iotabuild.zippyhand_Admin.jobs.repository.JobAssignmentRepository
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.wallet.service.WalletService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import com.iotabuild.inAppOTP.OtpService

@Service
class PartnerJobService(
    private val repository: JobAssignmentRepository,
    private val orderRepository: OrderRepository,
    private val otpService: OtpService,
    private val walletService: WalletService,
    private val passwordEncoder: PasswordEncoder
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
        val assignment = repository.findById(id).orElseThrow { Exception("Assignment not found") }
        if (assignment.uid != uid) throw Exception("Unauthorized to view this job")
        return toDetailsResponse(assignment)
    }

    fun acceptJob(assignmentId: Long, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { Exception("Assignment not found") }
        
        if (assignment.uid != uid) throw Exception("Unauthorized to perform this action")
        
        if (assignment.status == "CANCELLED") {
            throw IllegalStateException("This booking has been cancelled by the customer.")
        }
        if (assignment.status != "PENDING") {
            throw IllegalStateException("This booking is no longer pending (Current status: ${assignment.status}).")
        }

        // Double check base order as a fail-safe
        val orderOptional = orderRepository.findByOrderId(assignment.orderId)
        if (orderOptional.isPresent && orderOptional.get().status == com.iotabuild.zippyhand_Admin.order.entity.OrderStatus.CANCELLED) {
             throw IllegalStateException("This booking has been cancelled by the customer.")
        }

        assignment.status = "ACCEPTED"
        assignment.acceptedAt = LocalDateTime.now()

        if (orderOptional.isPresent) {
            val order = orderOptional.get()
            if (order.status == com.iotabuild.zippyhand_Admin.order.entity.OrderStatus.ASSIGNED) {
                order.status = com.iotabuild.zippyhand_Admin.order.entity.OrderStatus.IN_PROGRESS
                orderRepository.save(order)
            }
        }

        return toDetailsResponse(repository.save(assignment))
    }

    fun rejectJob(assignmentId: Long, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { Exception("Assignment not found") }
        
        if (assignment.uid != uid) throw Exception("Unauthorized to perform this action")
        
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
        val assignment = repository.findById(assignmentId).orElseThrow { Exception("Assignment not found") }
        if (assignment.uid != uid) throw Exception("Unauthorized to perform this action")
        
        return "Please ask the customer for the OTP displayed in their Zippyhand mobile app."
    }

    @Transactional
    fun completeJob(assignmentId: Long, otp: String, uid: String): JobDetailsResponse {
        val assignment = repository.findById(assignmentId).orElseThrow { Exception("Assignment not found") }
        if (assignment.uid != uid) throw Exception("Unauthorized to perform this action")
        if (assignment.status == "COMPLETED") throw Exception("Job is already completed")
        
        val order = orderRepository.findByOrderId(assignment.orderId)
            .orElseThrow { Exception("Order not found") }

        val otpRecord = otpService.getOtpRecord(order.orderId)
            ?: throw Exception("No active OTP found")

        val isOtpValid = passwordEncoder.matches(otp, otpRecord.otpHash)
        if (!isOtpValid) {
            logger.warn("Invalid OTP attempt for order ${order.orderId}")
            throw Exception("Invalid OTP provided")
        }
            
        assignment.status = "COMPLETED"
        assignment.completedAt = LocalDateTime.now()
        repository.save(assignment)

        order.status = com.iotabuild.zippyhand_Admin.order.entity.OrderStatus.COMPLETED
        order.completedAt = LocalDateTime.now()
        orderRepository.save(order)

        val totalPrice = BigDecimal(order.totalAmount)
        val commission = totalPrice.multiply(BigDecimal("0.10"))
        walletService.recordJobEarning(uid, order.orderId, totalPrice, commission)
        
        logger.info("Job completed successfully for order ${order.orderId}")
        
        return toDetailsResponse(assignment)
    }
    
    private fun toListItemResponse(assignment: JobAssignment): JobListItemResponse {
        val order = orderRepository.findByOrderId(assignment.orderId).orElse(null)
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
        val order = orderRepository.findByOrderId(assignment.orderId).orElse(null)
        val totalPrice = order?.totalAmount ?: 0
        val commission = (totalPrice * 0.1).toInt()
        val payout = totalPrice - commission

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
}
