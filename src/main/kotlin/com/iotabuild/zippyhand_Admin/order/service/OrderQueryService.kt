package com.iotabuild.zippyhand_Admin.order.service

import com.iotabuild.inAppOTP.OtpService
import com.iotabuild.zippyhand_Admin.jobs.repository.JobAssignmentRepository
import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import com.iotabuild.zippyhand_Admin.order.entity.OrderEntity
import com.iotabuild.zippyhand_Admin.order.mapper.OrderMapper
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
    private val jobAssignmentRepository: JobAssignmentRepository,
    private val professionalRepository: ProfessionalRepository,
    private val otpService: OtpService,
    private val orderMapper: OrderMapper
) {

    fun getOrder(id: Long): OrderResponse {
        val entity = orderRepository.findById(id).orElseThrow { RuntimeException("Order not found") }
        return entityToResponse(entity)
    }

    fun getOrderEntity(id: Long): OrderEntity {
        return orderRepository.findById(id).orElseThrow { RuntimeException("Order not found") }
    }

    fun getOrderEntityByExternalId(orderId: String): OrderEntity {
        return orderRepository.findByOrderId(orderId).orElseThrow { RuntimeException("Order not found") }
    }

    fun getOrdersByCustomer(customerUid: String, pageable: Pageable): Page<OrderResponse> {
        return orderRepository.findByCustomerId(customerUid, pageable).map { entityToResponse(it) }
    }

    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll().map { entityToResponse(it) }
    }

    /**
     * Internal logic to convert Entity to Response with all stitched data.
     */
    private fun entityToResponse(order: OrderEntity): OrderResponse {
        val latestAssignment = jobAssignmentRepository.findAllByOrderId(order.orderId)
            .filter { it.status !in listOf("REJECTED", "CANCELLED") }
            .maxByOrNull { it.assignedAt }

        val isAdmin = SecurityContextHolder.getContext().authentication?.authorities
            ?.contains(SimpleGrantedAuthority("ROLE_ADMIN")) == true

        // Rules for showing technician identity
        val showTechIdentity = isAdmin || latestAssignment?.status in listOf("ACCEPTED", "COMPLETED")
        
        val technician = if (showTechIdentity && latestAssignment != null) {
            professionalRepository.findById(latestAssignment.professionalId).orElse(null)
        } else null

        // OTP visibility
        val decryptedOtp = if (showTechIdentity && latestAssignment != null) {
            otpService.decryptOtpForCustomer(order.orderId)
        } else null
        
        return orderMapper.toResponse(order, technician, decryptedOtp)
    }
}
