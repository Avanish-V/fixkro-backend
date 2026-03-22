package com.iotabuild.zippyhand_Admin.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByCustomerId(customerId: String,pageable: Pageable): Page<Order>
    fun findByOrderId(orderId: String): Optional<Order>
}
