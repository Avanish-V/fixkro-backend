package com.iotabuild.zippyhand_Admin.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    fun findByOrderId(orderId: String): Optional<Payment>

    fun findByCfOrderId(cfOrderId: String): Optional<Payment>

    fun findByCfPaymentId(cfPaymentId: String): Optional<Payment>

    fun existsByOrderId(orderId: String): Boolean
}
