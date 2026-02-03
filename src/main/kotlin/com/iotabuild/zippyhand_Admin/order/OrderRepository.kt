package com.iotabuild.zippyhand_Admin.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, Long>{
    //fun getAllOrdersByUserId(userId: UUID): List<Order>
}
