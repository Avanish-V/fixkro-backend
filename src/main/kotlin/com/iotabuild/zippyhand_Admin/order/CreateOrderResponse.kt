package com.iotabuild.zippyhand_Admin.order

import java.math.BigDecimal

data class CreateOrderResponse(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val status: OrderStatus,
    val totalAmount: Int
)
