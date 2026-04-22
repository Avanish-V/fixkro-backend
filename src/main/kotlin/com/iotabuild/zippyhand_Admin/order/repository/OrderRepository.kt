package com.iotabuild.zippyhand_Admin.order.repository

import com.iotabuild.zippyhand_Admin.order.entity.OrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findByCustomerId(customerId: String, pageable: Pageable): Page<OrderEntity>
    fun findByOrderId(orderId: String): Optional<OrderEntity>
}
