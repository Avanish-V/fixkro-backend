package com.iotabuild.zippyhand_Admin.payment.repository

import com.iotabuild.zippyhand_Admin.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Optional<Payment>
    fun findByCfOrderId(cfOrderId: String): Optional<Payment>
}
