package com.iotabuild.zippyhand_Admin.common.dashboard

import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import java.time.LocalDateTime

data class DashboardStats(
    val totalOrders: Long,
    val totalRevenue: Long,
    val activeServices: Long,
    val totalProfessionals: Long,
    val completedToday: Long,
    val inProgress: Long,
    val pending: Long,
    val growthRate: String,
    val revenueData: List<ChartData>,
    val recentOrders: List<OrderResponse>
)

data class ChartData(
    val name: String,
    val revenue: Long,
    val orders: Long
)
