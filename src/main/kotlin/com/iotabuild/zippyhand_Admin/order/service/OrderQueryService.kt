package com.iotabuild.zippyhand_Admin.order.service

import com.iotabuild.inAppOTP.OtpService
import com.iotabuild.zippyhand_Admin.jobs.service.PartnerJobService
import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import com.iotabuild.zippyhand_Admin.order.entity.OrderEntity
import com.iotabuild.zippyhand_Admin.order.mapper.OrderMapper
import com.iotabuild.zippyhand_Admin.order.repository.OrderRepository
import com.iotabuild.zippyhand_Admin.payment.dto.toStatusResponse
import com.iotabuild.zippyhand_Admin.payment.repository.PaymentRepository
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalQueryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
    private val partnerJobService: PartnerJobService,
    private val professionalQueryService: ProfessionalQueryService,
    private val otpService: OtpService,
    private val paymentRepository: PaymentRepository,
    private val orderMapper: OrderMapper
) {

    @Transactional(readOnly = true)
    fun getOrder(id: Long): OrderResponse {
        val entity = orderRepository.findById(id).orElseThrow { RuntimeException("Order not found") }
        return entityToResponse(entity)
    }

    @Transactional(readOnly = true)
    fun getOrderEntity(id: Long): OrderEntity {
        return orderRepository.findById(id).orElseThrow { RuntimeException("Order not found") }
    }

    @Transactional(readOnly = true)
    fun getOrderEntityByExternalId(orderId: String): OrderEntity {
        return orderRepository.findByOrderId(orderId).orElseThrow { RuntimeException("Order not found") }
    }

    @Transactional(readOnly = true)
    fun getOrdersByCustomer(customerUid: String, pageable: Pageable): Page<OrderResponse> {
        return orderRepository.findByCustomerId(customerUid, pageable).map { entityToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll().map { entityToResponse(it) }
    }

    /**
     * Internal logic to convert Entity to Response with all stitched data.
     */
    private fun entityToResponse(order: OrderEntity): OrderResponse {
        val latestAssignment = partnerJobService.getActiveAssignmentForOrder(order.orderId)

        val isAdmin = SecurityContextHolder.getContext().authentication?.authorities
            ?.contains(SimpleGrantedAuthority("ROLE_ADMIN")) == true

        // Rules for showing technician identity
        val showTechIdentity = isAdmin || latestAssignment?.status in listOf("ACCEPTED", "COMPLETED")
        
        val technician = if (showTechIdentity && latestAssignment != null) {
            try {
                professionalQueryService.getProfessionalById(latestAssignment.professionalId)
            } catch (e: Exception) {
                null
            }
        } else null

        // OTP visibility
        val decryptedOtp = if (showTechIdentity && latestAssignment != null) {
            otpService.decryptOtpForCustomer(order.orderId)
        } else null
        
        val paymentDetails = paymentRepository.findByOrderId(order.orderId)
            .map { it.toStatusResponse() }
            .orElse(null)

        return orderMapper.toResponse(order, technician, decryptedOtp, paymentDetails)
    }
}
