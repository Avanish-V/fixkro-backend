package com.iotabuild.zippyhand_Admin.order

import java.math.BigDecimal

data class CreateOrderResponse(
    val orderId: Long,
    val status: OrderStatus,
    val totalAmount: Int
)
